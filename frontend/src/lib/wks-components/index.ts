/**
 * wks-components — case-management UI primitives.
 *
 * The reference UI in this repo is built entirely from these components.
 * Same code SIs consume when they embed WKS Platform in their own apps.
 *
 * Packaging as a standalone npm module is post-MVP; today this is an
 * internal barrel that already defines the public contract.
 */
export { ActivityTab } from './ActivityTab';
export { CaseDetailBody } from './CaseDetailBody';
export {
  ASSIGNEE_FIELD_ID,
  CasesTable,
  STATUS_FIELD_ID,
  type InlineEditTarget,
} from './CasesTable';
export { DocumentsTab } from './DocumentsTab';
export {
  InlineFieldEditor,
  InlineSelectEditor,
  InlineTextEditor,
  isInlineEditableField,
} from './InlineEditCell';
export { PropertiesTab } from './PropertiesTab';
export { StageTimeline } from './StageTimeline';
export { TasksTab } from './TasksTab';
