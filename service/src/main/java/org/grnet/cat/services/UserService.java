package org.grnet.cat.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.UriInfo;
import org.grnet.cat.dtos.UserProfileDto;
import org.grnet.cat.dtos.ValidationRequest;
import org.grnet.cat.dtos.ValidationResponse;
import org.grnet.cat.dtos.pagination.PageResource;
import org.grnet.cat.entities.User;
import org.grnet.cat.entities.Validation;
import org.grnet.cat.enums.Source;
import org.grnet.cat.enums.UserType;
import org.grnet.cat.enums.ValidationStatus;
import org.grnet.cat.exceptions.ConflictException;
import org.grnet.cat.mappers.UserMapper;
import org.grnet.cat.mappers.ValidationMapper;
import org.grnet.cat.repositories.ActorRepository;
import org.grnet.cat.repositories.UserRepository;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * The UserService provides operations for managing User entities.
 */

@ApplicationScoped
public class UserService {

    /**
     * Injection point for the User Repository
     */
    @Inject
    UserRepository userRepository;

    /**
     * Injection point for the Validation Service
     */
    @Inject
    ValidationService validationService;

    /**
     * Injection point for the Actor repository
     */
    @Inject
    ActorRepository actorRepository;

    /**
     * Get User's profile.
     *
     * @param id User's voperson_id
     * @return User's Profile.
     */
    public UserProfileDto getUserProfile(String id){

        var userProfile = userRepository.fetchUser(id);

        return UserMapper.INSTANCE.userToProfileDto(userProfile);
    }

    /**
     * Retrieves a page of users from the database.
     *
     * @param page The index of the page to retrieve (starting from 0).
     * @param size The maximum number of users to include in a page.
     * @param uriInfo The Uri Info.
     * @return A list of UserProfileDto objects representing the users in the requested page.
     */
    public PageResource<UserProfileDto> getUsersByPage(int page, int size, UriInfo uriInfo){

        var users = userRepository.fetchUsersByPage(page, size);

        return new PageResource<>(users, UserMapper.INSTANCE.usersProfileToDto(users.list()), uriInfo);
    }

    /**
     * Updates the metadata for a user's profile.
     *
     * @param id The ID of the user whose metadata is being updated.
     * @param name The user's name.
     * @param surname The user's surname.
     * @param email The user's email address.
     * @param orcidId The user's orcid id.
     * @return The updated user's profile
     */
    public UserProfileDto updateUserProfileMetadata(String id, String name, String surname, String email, String orcidId){

        var user = userRepository.updateUserMetadata(id, name, surname, email, orcidId);

        return UserMapper.INSTANCE.userToProfileDto(user);
    }

    /**
     * This operations registers a user on the CAT service.
     * Typically, it constructs an {@link User Identified} object and stores it in the database.
     * @param id User's voperson_id
     */
    @Transactional
    public UserProfileDto register(String id){

        var optionalUser = userRepository.searchByIdOptional(id);

        optionalUser.ifPresent(s -> {throw new ConflictException("User already exists in the database.");});

        var identified = new User();
        identified.setId(id);
        identified.setRegisteredOn(Timestamp.from(Instant.now()));

        userRepository.persist(identified);

        return UserMapper.INSTANCE.userToProfileDto(identified);
    }

    /**
     * Requests user promotion to become a validated user.
     *
     * @param id The ID of the identified user requesting promotion.
     * @param validationRequest The promotion request information.
     * @return The submitted validation requesgt.
     */
    @Transactional
    public ValidationResponse validate(String id, ValidationRequest validationRequest){

        validationService.hasPromotionRequest(id, validationRequest);

        var user = userRepository.findById(id);

        var actor = actorRepository.findById(validationRequest.actorId);

        var validation = new Validation();

        validation.setUser(user);
        validation.setActor(actor);
        validation.setCreatedOn(Timestamp.from(Instant.now()));
        validation.setStatus(ValidationStatus.REVIEW);
        validation.setOrganisationId(validationRequest.organisationId);
        validation.setOrganisationName(validationRequest.organisationName);
        validation.setOrganisationSource(Source.valueOf(validationRequest.organisationSource));
        validation.setOrganisationWebsite(validationRequest.organisationWebsite);
        validation.setOrganisationRole(validationRequest.organisationRole);

        validationService.store(validation);

        return ValidationMapper.INSTANCE.validationToDto(validation);
    }

    /**
     * Delete identified users from the database.
     *
     */
    @Transactional
    public void deleteAll(){

        userRepository.deleteAll();
    }
}