// Centralise les constantes sensibles (rôles/statuts/transitions) du workflow.
// Objectif: éviter les fautes de frappe dans les chaînes envoyées au backend.

export const FrontRoles = {
  Abo: 'abo',
  As: 'as',
  Resp: 'resp',
  Af: 'af',
  Admin: 'admin',
} as const

export const BackendRoles = {
  AGENT_BUREAU_ORDRE: 'AGENT_BUREAU_ORDRE',
  AGENT_SERVICE: 'AGENT_SERVICE',
  RESPONSABLE_HIERARCHIQUE: 'RESPONSABLE_HIERARCHIQUE',
  AGENT_FINANCIER: 'AGENT_FINANCIER',
  ADMIN_SYSTEME: 'ADMIN_SYSTEME',
} as const

export const WorkflowStatuts = {
  RECU: 'RECU',
  ENREGISTRE: 'ENREGISTRE',
  EN_TRAITEMENT: 'EN_TRAITEMENT',
  VALIDE: 'VALIDE',
  REJETE: 'REJETE',
  PAYE: 'PAYE',
  ARCHIVE: 'ARCHIVE',
  OUVERT: 'OUVERT',
  EN_COURS: 'EN_COURS',
} as const

// Alias utiles côté UI lors des transitions.
export const WorkflowTransitions = {
  TRANSIT_AS_TO_RESP_VALIDATION: WorkflowStatuts.VALIDE,
  TRANSIT_AS_TO_RESP_REJECTION: WorkflowStatuts.REJETE,
  TRANSIT_AS_TO_RESP_TREATMENT: WorkflowStatuts.EN_TRAITEMENT,
  TRANSIT_RESP_TO_FINANCE_PAYMENT: WorkflowStatuts.PAYE,
  TRANSIT_BO_TO_ARCHIVE: WorkflowStatuts.ARCHIVE,
} as const

