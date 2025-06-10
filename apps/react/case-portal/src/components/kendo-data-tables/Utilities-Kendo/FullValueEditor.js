import React from 'react'

const FullValueEditor = ({ dataItem, field, onChange }) => {
  const handleChange = (e) => {
    const value = e.target.value
    // Keep value as string, but convert to number if you want numeric only
    onChange({
      dataItem,
      field,
      value: value === '' ? null : Number(value),
    })
  }

  return (
    <td>
      <input
        type='number'
        step='any'
        value={dataItem[field] ?? ''}
        onChange={handleChange}
        style={{ width: '100%' }}
      />
    </td>
  )
}

export default FullValueEditor
