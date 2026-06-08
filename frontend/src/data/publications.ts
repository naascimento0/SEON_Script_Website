export interface Publication {
  year: number
  ontology?: string
  reference: string
  link?: string
}

export const seonOntologyPublications: Publication[] = [
  {
    year: 2005,
    ontology: 'UFO',
    reference: 'Guizzardi, G. (2005). <em>Ontological foundations for structural conceptual models</em>. Ph.D. Thesis, Netherlands, Universal Press.',
    link: 'https://nemo.inf.ufes.br/wp-content/papercite-data/pdf/ontological_foundations_for_structural_conceptual_models_2005.pdf',
  },
  {
    year: 2010,
    ontology: 'UFO',
    reference: 'Guizzardi, G. & Wagner, G. (2010). Using the unified foundational ontology (UFO) as a foundation for general conceptual modeling languages. In <em>Theory and Applications of Ontology: Computer Applications</em>. Springer.',
  },
  {
    year: 2017,
    ontology: 'ROoST',
    reference: 'De Souza, É. F., De Almeida Falbo, R., and Vijaykumar, N. L. (2017). ROoST: Reference ontology on software testing. <em>Applied Ontology</em>, Vol. 12, Number 1.',
    link: 'https://nemo.inf.ufes.br/wp-content/papercite-data/pdf/roost__reference_ontology_on_software_testing_2017.pdf',
  },
  {
    year: 2018,
    ontology: 'SwO, RSRO, RRO',
    reference: 'Duarte, B. B., de Castro Leal, A. L., de Almeida Falbo, R., Guizzardi, G., Guizzardi, R. S. S., & Souza, V. E. S. (2018). Ontological foundations for software requirements with a focus on requirements at runtime. <em>Applied Ontology</em>, 13(2), 73–105. doi:10.3233/AO-180197',
    link: 'https://journals.sagepub.com/doi/10.3233/AO-180197',
  },
  {
    year: 2018,
    ontology: 'SPMO',
    reference: 'Bastos, E. C., Barcellos, M. P., and De Almeida Falbo, R. (2018). Using Semantic Documentation to Support Software Project Management. <em>Journal on Data Semantics</em>, vol. 7, p. 107–132.',
    link: 'https://www.inf.ufes.br/~monalessa/wp-content/papercite-data/pdf/using_semantic_documentation_to_support_software_project_management_2018.pdf',
  },
  {
    year: 2018,
    ontology: 'OSDEF',
    reference: 'Falbo, R., Duarte, B., and Guizzardi, G. (2018). Towards an Ontology of Software Defects, Errors and Failures. doi:10.1007/978-3-030-00847-5_25.',
    link: 'https://www.academia.edu/36919969/Towards_an_Ontology_of_Software_Defects_Errors_and_Failures',
  },
  {
    year: 2019,
    ontology: 'MTO',
    reference: 'Santos, L. A., Barcellos, M. P., De Almeida Falbo, R., Reginato, C. C., and Campos, P. M. C. (2019). Measurement Task Ontology. In <em>12th Seminar on Ontology Research in Brazil (Ontobras 2019)</em> – CEUR Workshop Proceedings.',
    link: 'https://www.inf.ufes.br/~monalessa/wp-content/papercite-data/pdf/measurement_task_ontology_2019.pdf',
  },
  {
    year: 2019,
    ontology: 'OOC-O',
    reference: 'de Aguiar, C. Z., de Almeida Falbo, R., and Souza, V. E. S. (2019). OOC-O: A Reference Ontology on Object-Oriented Code.',
    link: 'https://www.inf.ufes.br/~vitorsouza/wp-content/papercite-data/pdf/aguiar-et-al-er19.pdf',
  },
  {
    year: 2021,
    ontology: 'SDRO',
    reference: 'Castro, M. V. H. B., Barcellos, M. P., and Falbo, R. A. (2021). An Ontological View of Design in the Software Context. In <em>14th Seminar on Ontology Research in Brazil (ONTOBRAS)</em>.',
    link: 'https://www.inf.ufes.br/~monalessa/wp-content/papercite-data/pdf/an_ontological_view_of_design_in_the_software_context_2021.pdf',
  },
  {
    year: 2021,
    ontology: 'SRO',
    reference: 'SantosJr, P. S., Barcellos, M. P., de Falbo, R. A., and Almeida, J. P. A. (2021). From a Scrum Reference Ontology to the Integration of Applications for Data-Driven Software Development. <em>Information and Software Technology</em>, vol. 136.',
    link: 'https://www.inf.ufes.br/~monalessa/wp-content/papercite-data/pdf/from_a_scrum_reference_ontology_to_the_integration_of_applications_for_data_driven_software_development_2021.pdf',
  },
  {
    year: 2021,
    ontology: 'OSDEF, ROSS',
    reference: 'Duarte, B. B., de Falbo, R. A., Guizzardi, G., Guizzardi, R., and Souza, V. E. S. (2021). An ontological analysis of software system anomalies and their associated risks. <em>Data & Knowledge Engineering</em>, vol. 134, p. 101892.',
    link: 'https://www.inf.ufes.br/~vitorsouza/wp-content/papercite-data/pdf/duarte-et-al-dke21.pdf',
  },
  {
    year: 2023,
    ontology: 'Continuum Ontology Network',
    reference: 'dos Santos Júnior, P. S., Almeida, J. P. A., and Barcellos, M. P. (2023). Towards Federated Ontology-Driven Data Integration in Continuous Software Engineering. In <em>Proceedings of SBES 2023</em>, p. 31–36.',
    link: 'https://www.inf.ufes.br/~monalessa/wp-content/papercite-data/pdf/towards_federated_ontology_driven_data_integration_in_continuous_software_engineering_2023.pdf',
  },
  {
    year: 2023,
    ontology: 'DepIn-O',
    reference: 'Guterres, C. S., de Aguiar, C. Z., and Souza, V. E. S. (2023). DepIn-O: an Ontology on Dependency Injection Software Frameworks. In <em>Proc. of the 16th Seminar on Ontology Research in Brazil (ONTOBRAS 2023)</em>, Brasília, DF, Brasil.',
    link: 'https://www.inf.ufes.br/~vitorsouza/wp-content/papercite-data/pdf/guterres-et-al-ontobras23.pdf',
  },
]

export const generalSeonPublications: Publication[] = [
  {
    year: 2016,
    reference: 'Ruy, F., Falbo, R. A., Barcellos, M. P., Costa, S. D., and Guizzardi, G. (2016). SEON: A software engineering ontology network. In <em>20th International Conference on Knowledge Engineering and Knowledge Management</em>.',
    link: 'https://www.inf.ufes.br/~monalessa/wp-content/papercite-data/pdf/seon__a_software_engineering_ontology_network_2016.pdf',
  },
  {
    year: 2017,
    reference: 'Ruy, F. B., Souza, É., De Almeida Falbo, R., and Barcellos, M. P. (2017). Software Testing Processes in ISO Standards: How to Harmonize Them? In <em>XVI Brazilian Symposium on Software Quality (SBQS 2017)</em>.',
    link: 'https://www.inf.ufes.br/~monalessa/wp-content/papercite-data/pdf/software_testing_processes_in_iso_standards__how_to_harmonize_them__2017.pdf',
  },
  {
    year: 2017,
    reference: 'Detoni, A. A., Miranda, G. M., Renault, L. D. C., Falbo, R. A., Almeida, J. P. A., Guizzardi, G., and Barcellos, M. P. (2017). Exploring the Role of Enterprise Architecture Models in the Modularization of an Ontology Network: A Case in the Public Security Domain. In <em>2017 IEEE 21st International Enterprise Distributed Object Computing Workshop (EDOCW)</em>, p. 117–126.',
    link: 'https://nemo.inf.ufes.br/wp-content/papercite-data/pdf/exploring_the_role_of_enterprise_architecture_models_in_the_modularization_of_an_ontology_network__a_case_in_the_public_security_domain_2017.pdf',
  },
  {
    year: 2018,
    reference: 'Campos, P. M. C., Reginato, C. C., Almeida, J. P. A., Nardi, J. C., Barcellos, M. P., Falbo, R. A., and Guizzardi, R. S. S. (2018). Building an Ontology Network to Support Environmental Quality Research: First Steps. In <em>Proceedings of the XI Seminar on Ontology Research in Brazil (ONTOBRAS)</em>, p. 227–232.',
    link: 'https://nemo.inf.ufes.br/wp-content/papercite-data/pdf/building_an_ontology_network_to_support_environmental_quality_research__first_steps_2018.pdf',
  },
  {
    year: 2018,
    reference: 'Renault, L. D. C., Barcellos, M. P., and De Almeida Falbo, R. (2018). Using an Ontology-based Approach for Integrating Applications to support Software Processes. In <em>XVII Brazilian Symposium on Software Quality (SBQS 2018)</em>.',
    link: 'https://www.inf.ufes.br/~monalessa/wp-content/papercite-data/pdf/using_an_ontology_based_approach_for_integrating_applications_to_support_software_processes_2018.pdf',
  },
  {
    year: 2020,
    reference: 'dos SantosJr, P. S., Barcellos, M. P., and Calhau, R. F. (2020). Am I Going to Heaven? First Step Climbing the Stairway to Heaven Model – Results from a Case Study in Industry. In <em>34th Brazilian Symposium on Software Engineering (SBES 2020)</em>, p. 309–318.',
    link: 'https://www.inf.ufes.br/~monalessa/wp-content/papercite-data/pdf/am_i_going_to_heaven__first_step_climbing_the_stairway_to_heaven_model_2020.pdf',
  },
  {
    year: 2020,
    reference: 'Barcellos, M. P. (2020). Towards a Framework for Continuous Software Engineering. In <em>34th Brazilian Symposium on Software Engineering (SBES 2020)</em>, p. 626–631.',
    link: 'https://www.inf.ufes.br/~monalessa/wp-content/papercite-data/pdf/towards_a_framework_for_continuous_software_engineering_2020.pdf',
  },
  {
    year: 2021,
    reference: 'SantosJr, P. S., Barcellos, M. P., and Almeida, J. P. A. (2021). An Ontology-based Approach to enable Data-Driven Decision-Making in Agile Software Organizations. In <em>5th Doctoral and Masters Consortium on Ontologies – 14th Seminar on Ontology Research in Brazil (ONTOBRAS)</em>.',
    link: 'https://www.inf.ufes.br/~monalessa/wp-content/papercite-data/pdf/an_ontology_based_approach_to_enable_data_driven_decision_making_in_agile_software_organizations_2021.pdf',
  },
  {
    year: 2021,
    reference: 'Guizzardi, G., Benevides, A. B., Fonseca, C. M., Porello, D., Almeida, J. P. A., and Sales, T. P. (2022). UFO: Unified Foundational Ontology. <em>Applied Ontology</em>, vol. 17, p. 167–210.',
    link: 'https://nemo.inf.ufes.br/wp-content/uploads/ufo_unified_foundational_ontology_2021.pdf',
  },
  {
    year: 2022,
    reference: 'dos Santos Júnior, P. S., Perini Barcellos, M., and Fernandes Calhau, R. (2022). First Step Climbing the Stairway to Heaven Model – Results from a Case Study in Industry. <em>Journal of Software Engineering Research and Development</em>, vol. 9, iss. 1, p. 21:1–21:17.',
    link: 'https://www.inf.ufes.br/~monalessa/wp-content/papercite-data/pdf/first_step_climbing_the_stairway_to_heaven_model__results_from_a_case_study_in_industry_2022.pdf',
  },
  {
    year: 2023,
    reference: "Santos Júnior, P. S., Almeida, J. P. A., and Barcellos, M. (2023). Towards Federated Ontology-Driven Data Integration in Continuous Software Engineering. In <em>Proceedings of SBES '23</em>. ACM, New York, p. 31–36. doi:10.1145/3613372.3613380",
    link: 'https://nemo.inf.ufes.br/wp-content/papercite-data/pdf/towards_federated_ontology_driven_data_integration_in_continuous_software_engineering_2023.pdf',
  },
]
