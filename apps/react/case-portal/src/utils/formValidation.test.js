import { isEmpty, getNestedValue, isValidEmail } from './formValidation'

describe('isEmpty', () => {
  it.each([
    [undefined, true],
    [null, true],
    ['', true],
    [[], true],
    [{}, true],
    ['x', false],
    [0, false],
    [false, false],
    [[1], false],
    [{ a: 1 }, false],
  ])('isEmpty(%p) === %p', (value, expected) => {
    expect(isEmpty(value)).toBe(expected)
  })
})

describe('getNestedValue', () => {
  const obj = { a: { b: { c: 42 } }, x: null }

  it('resolves a dotted path', () => {
    expect(getNestedValue(obj, 'a.b.c')).toBe(42)
  })

  it('returns undefined for a missing path', () => {
    expect(getNestedValue(obj, 'a.b.z')).toBeUndefined()
  })

  it('returns undefined when traversing through null', () => {
    expect(getNestedValue(obj, 'x.y')).toBeUndefined()
  })

  it('returns undefined for empty inputs', () => {
    expect(getNestedValue(null, 'a')).toBeUndefined()
    expect(getNestedValue(obj, '')).toBeUndefined()
  })
})

describe('isValidEmail', () => {
  it.each([
    ['user@example.com', true],
    ['a.b-c@sub.domain.io', true],
    ['no-at-sign', false],
    ['missing@domain', false],
    ['spaces in@email.com', false],
    ['', false],
  ])('isValidEmail(%p) === %p', (email, expected) => {
    expect(isValidEmail(email)).toBe(expected)
  })
})
