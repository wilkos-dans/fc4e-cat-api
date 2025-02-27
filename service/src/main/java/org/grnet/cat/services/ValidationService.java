package org.grnet.cat.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.UriInfo;
import org.apache.commons.lang3.StringUtils;
import org.grnet.cat.dtos.ValidationRequest;
import org.grnet.cat.dtos.ValidationResponse;
import org.grnet.cat.dtos.pagination.PageResource;
import org.grnet.cat.entities.PageQuery;
import org.grnet.cat.entities.Validation;
import org.grnet.cat.enums.Source;
import org.grnet.cat.enums.ValidationStatus;
import org.grnet.cat.exceptions.ConflictException;
import org.grnet.cat.mappers.ValidationMapper;
import org.grnet.cat.repositories.ActorRepository;
import org.grnet.cat.repositories.ValidationRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * The ValidationService provides operations for managing Validation entities.
 */

@ApplicationScoped
public class ValidationService {

    @Inject
    ValidationRepository validationRepository;

    @Inject
    ActorRepository actorRepository;

    @Inject
    @Named("keycloak-service")
    RoleService roleService;

    BiConsumer<String, ValidationStatus> handleValidationStatus = (userId, status)->{

        switch (status){
            case APPROVED:{
                roleService.assignRolesToUser(userId, List.of("validated"));
                break;
            } default:{

            }
        }
    };

    /**
     * Checks if there is a promotion request for a specific user and organization.
     *
     * @param userId  The ID of the user.
     * @param request The promotion request information.
     * @throws ConflictException  if a promotion request exists for the user and organization.
     */
    public void hasPromotionRequest(String userId, ValidationRequest request) {

        // Call the repository method to check if a promotion request exists
        var exists = validationRepository.hasPromotionRequest(userId, request.organisationId, request.organisationSource, request.actorId);

        if(exists){
            throw new ConflictException("There is a promotion request for this user and organisation.");
        }
    }

    /**
     * Stores a new promotion request in database.
     *
     * @param validation The promotion request to be persisted in database.
     */
    @Transactional
    public void store(Validation validation){

        validationRepository.persist(validation);
    }

    /**
     * Retrieves a page of validation requests submitted by the specified user.
     *
     * @param page The index of the page to retrieve (starting from 0).
     * @param size The maximum number of validation requests to include in a page.
     * @param uriInfo The Uri Info.
     * @param userID The ID of the user.
     * @param status Validation status to search for.
     * @return A list of ValidationResponse objects representing the submitted promotion requests in the requested page.
     */
    public PageResource<ValidationResponse> getValidationsByUserAndPage(int page, int size, String status, UriInfo uriInfo, String userID){

        PageQuery<Validation> validations = null;

        if(StringUtils.isNotEmpty(status)) {

            validations = validationRepository.fetchValidationsByUserAndStatusAndPage(ValidationStatus.valueOf(status), page, size, userID);
        } else {

            validations = validationRepository.fetchValidationsByUserAndPage(page, size, userID);
        }

        return new PageResource<>(validations, ValidationMapper.INSTANCE.validationsToDto(validations.list()), uriInfo);
    }

    /**
     * Retrieves a page of validation requests submitted by users.
     *
     * @param page The index of the page to retrieve (starting from 0).
     * @param size The maximum number of validation requests to include in a page.
     * @param uriInfo The Uri Info.
     * @param status Validation status to search for.
     * @return A list of ValidationResponse objects representing the submitted promotion requests in the requested page.
     */
    public PageResource<ValidationResponse> getValidationsByPage(int page, int size, String status, UriInfo uriInfo){

        PageQuery<Validation> validations = null;

        if(StringUtils.isNotEmpty(status)){

            validations = validationRepository.fetchValidationsByStatusAndPage(ValidationStatus.valueOf(status), page, size);
        } else {

            validations = validationRepository.fetchValidationsByPage(page, size);
        }

        return new PageResource<>(validations, ValidationMapper.INSTANCE.validationsToDto(validations.list()), uriInfo);
    }

    /**
     * Updates the validation request with the provided details.
     * @param id The ID of the validation request to update.
     * @param validationRequest The updated validation request object.
     * @return The updated validation request.
     */
    @Transactional
    public ValidationResponse updateValidationRequest(Long id, ValidationRequest validationRequest){

        var validation = validationRepository.findById(id);

        var actor = actorRepository.findById(validationRequest.actorId);

        validation.setOrganisationWebsite(validationRequest.organisationWebsite);
        validation.setOrganisationName(validationRequest.organisationName);
        validation.setOrganisationId(validationRequest.organisationId);
        validation.setOrganisationRole(validationRequest.organisationRole);
        validation.setOrganisationSource(Source.valueOf(validationRequest.organisationSource));
        validation.setActor(actor);

        return ValidationMapper.INSTANCE.validationToDto(validation);
    }

    /**
     * Updates the status of a validation request with the provided status.
     * @param id The ID of the validation request to update.
     * @param status The new status to set for the validation request.
     * @param userId The user who validates a validation request.
     * @return The updated validation request.
     */
    @Transactional
    public ValidationResponse updateValidationRequestStatus(Long id, ValidationStatus status, String userId) {

         validationRepository.update("status = ?1 , validatedBy = ?2 ,validatedOn = ?3  where id = ?4", status,  userId,Timestamp.from(Instant.now()), id);

         var validation = validationRepository.findById(id);
         handleValidationStatus.accept(validation.getUser().getId(), status);

        return ValidationMapper.INSTANCE.validationToDto(validation);
    }

    /**
     * Retrieves a specific validation request if it belongs to the user.
     *
     * @param userId The ID of the user.
     * @param validationId The ID of the validation request to retrieve.
     * @return The validation request if it belongs to the user.
     * @throws ForbiddenException If the user is not authorized to access the validation request.
     */
    public ValidationResponse getValidationRequest(String userId, Long validationId) {

        var validation = validationRepository.findById(validationId);

        if(!validation.getUser().getId().equals(userId)){
            throw new ForbiddenException("Not Permitted.");
        }

        return ValidationMapper.INSTANCE.validationToDto(validation);
    }

    /**
     * Retrieves a specific validation request.
     *
     * @param validationId The ID of the validation request to retrieve.
     * @return The validation request if it belongs to the user.
     */
    public ValidationResponse getValidationRequest(Long validationId) {

        var validation = validationRepository.findById(validationId);

        return ValidationMapper.INSTANCE.validationToDto(validation);
    }

    @Transactional
    public void deleteAll(){
        validationRepository.deleteAll();
    }
}
