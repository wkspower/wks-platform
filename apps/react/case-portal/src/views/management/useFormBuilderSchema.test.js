import { act, render } from '@testing-library/react'
import { renderHook } from '@testing-library/react'
import FormBuilder from '@formio/react/lib/components/FormBuilder'
import { useFormBuilderSchema } from './useFormBuilderSchema'

// formiojs only supplies the *default* Builder implementation; every test here
// injects its own through FormBuilder's `Builder` prop, so the core never runs.
// Stubbing it keeps the heavyweight (and jsdom-hostile) import out of the way
// while the real @formio/react wrapper — the code whose contract we care about —
// stays untouched.
jest.mock('formiojs', () => ({ FormBuilder: class {} }))

let lastBuilder = null

/**
 * Minimal stand-in for a formiojs builder: same surface @formio/react drives
 * (ready / instance / on / off / setForm / setDisplay / destroy).
 */
class FakeBuilder {
  constructor(element, form) {
    lastBuilder = this
    this.handlers = {}
    const self = this
    this.instance = {
      form: { ...form },
      get schema() {
        return self.instance.form
      },
      on: (name, fn) => {
        self.handlers[name] = [...(self.handlers[name] || []), fn]
      },
      off: (name, fn) => {
        self.handlers[name] = (self.handlers[name] || []).filter(
          (h) => h !== fn,
        )
      },
      destroy: () => {},
    }
    this.ready = Promise.resolve()
  }

  setDisplay() {}

  setForm(form) {
    this.instance.form = { ...form }
  }

  /** What a drag-and-drop into the designer amounts to. */
  dropComponent(component) {
    this.instance.form = {
      ...this.instance.form,
      components: [...(this.instance.form.components || []), component],
    }
    ;(this.handlers['addComponent'] || []).forEach((fn) => fn())
  }
}

const flushReady = async () => {
  await act(async () => {
    await Promise.resolve()
  })
}

beforeEach(() => {
  lastBuilder = null
})

describe('@formio/react FormBuilder contract', () => {
  it('does not write edits back into the form object it was given', async () => {
    const structure = { components: [], display: 'form' }
    render(<FormBuilder form={structure} Builder={FakeBuilder} />)
    await flushReady()

    act(() => lastBuilder.dropComponent({ key: 'firstName' }))

    // This is the whole bug: the caller's object stays empty, so a view that
    // saves its own state saves nothing the user designed.
    expect(structure.components).toEqual([])
  })

  it('reports edits through onChange, the only outbound path', async () => {
    const structure = { components: [], display: 'form' }
    const onChange = jest.fn()
    render(
      <FormBuilder
        form={structure}
        Builder={FakeBuilder}
        onChange={onChange}
      />,
    )
    await flushReady()

    act(() => lastBuilder.dropComponent({ key: 'firstName' }))

    const [, schema] = onChange.mock.calls[onChange.mock.calls.length - 1]
    expect(schema.components).toEqual([{ key: 'firstName' }])
  })
})

describe('useFormBuilderSchema', () => {
  const structure = { components: [], display: 'form' }

  it('returns the structure untouched when the builder reported nothing', () => {
    const { result } = renderHook(() => useFormBuilderSchema(true))
    expect(result.current.mergeBuilderSchema(structure)).toEqual(structure)
  })

  it('layers the builder components over the structure held in state', () => {
    const { result } = renderHook(() => useFormBuilderSchema(true))

    act(() =>
      result.current.onBuilderChange({}, { components: [{ key: 'age' }] }),
    )

    expect(
      result.current.mergeBuilderSchema({ ...structure, display: 'wizard' }),
    ).toEqual({ components: [{ key: 'age' }], display: 'wizard' })
  })

  it('drops the previous session when the dialog is reopened', () => {
    const { result, rerender } = renderHook(
      ({ open }) => useFormBuilderSchema(open),
      { initialProps: { open: true } },
    )

    act(() =>
      result.current.onBuilderChange({}, { components: [{ key: 'age' }] }),
    )
    rerender({ open: false })
    rerender({ open: true })

    // A builder that outlived the close must not leak its components into the
    // next record edited.
    expect(result.current.mergeBuilderSchema(structure)).toEqual(structure)
  })
})
