import { useCallback, useEffect, useRef } from 'react'

/**
 * Bridges @formio/react's <FormBuilder> back into the view that renders it.
 *
 * FormBuilder never touches the object handed to its `form` prop — it builds on
 * a copy (`form = Object.assign({}, form)`) — so the edited schema only escapes
 * through `onChange`. A call site that omits `onChange` saves the untouched
 * initial structure and silently discards everything the user designed.
 *
 * The edited schema is kept in a ref rather than in state on purpose:
 * FormBuilder runs a layout effect on the `form` prop that calls `setForm()` on
 * the builder instance, so feeding a fresh object back on every drag would
 * rebuild the builder mid-edit.
 *
 * @param {boolean} open whether the editing dialog is open. Opening starts a new
 *   editing session, so anything captured from the previous one is dropped — the
 *   builder can outlive a close/reopen, or be reused for a different record.
 */
export const useFormBuilderSchema = (open) => {
  const schemaRef = useRef(null)

  useEffect(() => {
    if (open) {
      schemaRef.current = null
    }
  }, [open])

  // FormBuilder calls onChange(instanceForm, schema); `schema` is the cleaned
  // export and exposes `components` through a live getter.
  const onBuilderChange = useCallback((instanceForm, schema) => {
    schemaRef.current = schema || instanceForm
  }, [])

  /**
   * Layers the builder's components over the structure the view holds in state.
   * State stays authoritative for the fields the view controls itself (display,
   * title, ...); only `components` comes from the builder. With no edits
   * captured the structure is returned untouched.
   */
  const mergeBuilderSchema = useCallback((structure) => {
    const schema = schemaRef.current
    if (!schema) {
      return structure
    }
    return { ...structure, components: schema.components ?? [] }
  }, [])

  return { onBuilderChange, mergeBuilderSchema }
}
