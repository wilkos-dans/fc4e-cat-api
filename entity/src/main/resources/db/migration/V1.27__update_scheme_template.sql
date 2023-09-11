-- ------------------------------------------------
-- Version: v1.27
--
-- Description: Migration that updates the schme template json document in the Template table for version 8
-- -------------------------------------------------
UPDATE Template set template_doc ='{
"name": "",
"assessment_type": {
"id": 1,
"name": "eosc pid policy"
},
"version": "",
"status": "",
"published": false,
"timestamp": "",
"actor": {
"id": 9,
"name": "PID Scheme"
},
"organisation": {
"name": "",
"id": ""
},
"subject": {
"name": "",
"id": "",
"type": ""
},
"result": {
"compliance": null,
"ranking": null
},
"principles": [
{
"id": "P10",
"name": "Governance",
"criteria": [
{
"id": "C28",
"name": "Certification",
"metric": {
"type": "number",
"tests": [
{
"id": "T28",
"name": "Certification",
"text": "Are you declaring publicly that you are willing to get certified?",
"type": "binary",
"result": null,
"guidance": {
"id": "G28",
"description": "In practice, evaluation is very difficult, due to two factors: \\n - It requires that a sample of millions of PID owners be evaluated across all services, and \\n - Some entities may never have to be maintained and are, despite years of non-maintenance, up to date. \\n A measure of the mean update frequency of PIDs across a specific service, and monitoring its change over time against a benchmark, may be the only realistic assessment mechanism."
},
"description": "Public declaration of willingness to be certified",
"evidence_url": []
}
],
"value": null,
"result": null,
"algorithm": "sum",
"benchmark": {
"equal_greater_than": 1
}
},
"imperative": "must",
"description": "PID Authorities and Services MUST agree to be certified with a mutually agreed frequency in respect of policy compliance."
}
],
"description": "PID Service Providers should apply appropriate community governance to ensure that their PID Services and Systems adhere to these policies, and are agile and responsive to the needs of research, Open Science and EOSC."
},
{
"id": "P13",
"name": "Persistence",
"criteria": [

{
"id": "C34",
"name": "Persistence Mean",
"metric": {
"type": "number",
"tests": [
{
"id": "T34",
"name": "Persistence Mean",
"text": "Given a random statistically significant sample for a provider and determining a distribution of resolvable PIDs as a function of time since creation, evaluate a mean m against a norm n. Please provide the values for m and n",
"type": "value",
"value": null,
"threshold": null,
"value_name": "m",
"threshold_name": "n",
"benchmark": {
"equal_greater_than": "threshold"
},
"result": null,
"guidance": {
"id": "G34",
"description": "Authorities will usually state this fact prominently on their websites, or it may be contained in the published specification for the schema."
},
"description": "Published literature [63], [64] suggest that the mean time that PIDs remain resolvable (a measure of persistence) varies quite significantly with the service in question. A randomised, statistically significant sample needs to be evaluated on a periodic basis (annually?) to gauge the persistence of services.",
"evidence_url": []
}
],
"value": null,
"result": null,
"algorithm": "sum",
"benchmark": {
"equal_greater_than": 1
}
},
"imperative": "should",
"description": "PID Services SHOULD aim for a persistence median that is acceptable to and aligns with community and dependency expectations. "
}
],
"description": "Persistence description"
},

{
"id": "P9",
"name": "Resolution",
"criteria": [
{
"id": "C35",
"name": "Resolution Percentage",
"metric": {
"type": "number",
"tests": [
{
"id": "T35",
"name": "Resolution Percentage",
"text": "Given the percentage f of resolved PIDs that result in a viable entity, compared to a community expectation p. Please provide values for f and p. ",
"type": "value",
"value": null,
"threshold": null,
"threshold_name": "p",
"value_name": "f",
"benchmark": {
"equal_greater_than": "threshold"
},
"result": null,
"guidance": {
"id": "G1",
"description": "In practice, evaluation is very difficult, due to two factors: \\n - It requires that a sample of millions of PID owners be evaluated across all services, and \\n - Some entities may never have to be maintained and are, despite years of non-maintenance, up to date. \\n A measure of the mean update frequency of PIDs across a specific service, and monitoring its change over time against a benchmark, may be the only realistic assessment mechanism."
},
"description": "The test involves determining the percentage f of resolved PIDs that result in a viable entity, compared to a community expectation p.",
"evidence_url": []
}
],
"value": null,
"result": null,
"algorithm": "sum",
"benchmark": {
"equal_greater_than": 1
}
},
"imperative": "should",
"description": "The PID owner SHOULD maintain PID attributes."
}
],
"description": "PID Service SHOULD resolve at least p percent of PIDs in a randomised sample, where p is determined by community and dependency expectations."
}


]


}' where id=3;
