import React from 'react'
import { NoSpinnerNumericEditor } from './numbericColumns'
import { SelectCellEditor } from './SelectCellEditor'
import { BooleanCellEditor } from './BooleanCellEditor'
import { TextCellEditorUpdated } from './TextCellEditorUpdated'
import DateOnlyPicker from './DatePicker'
import DateTimePickerEditor from './DatePickeronSelectedYr'
import { DropDownList } from '@progress/kendo-react-dropdowns'
import { Checkbox } from '@mui/material'

const FormulaTextEditor = ({ dataItem, field, onChange }) => {
  const [localValue, setLocalValue] = React.useState('');
  const [prefix, setPrefix] = React.useState('');
  const [suffix, setSuffix] = React.useState('');
  const inputRef = React.useRef(null);

  React.useEffect(() => {
    const value = String(dataItem?.[field] || '');
    const match = value.match(/^([+/-]+\s*)(\d+(?:\.\d+)?)(\s*%?)?$/);
    if (match) {
      setPrefix(match[1] || '');
      setLocalValue(match[2] || '');
      setSuffix(match[3] || '');
    } else {
      setLocalValue(value);
    }
  }, [dataItem, field]);

  const handleChange = (e) => {
    const val = e.target.value;
    if (val === '' || /^\d*\.?\d*$/.test(val)) {
      setLocalValue(val);
    }
  };

  const valueRef = React.useRef(localValue)
  valueRef.current = localValue
  const prefixRef = React.useRef(prefix)
  prefixRef.current = prefix
  const suffixRef = React.useRef(suffix)
  suffixRef.current = suffix
  const onChangeRef = React.useRef(onChange)
  onChangeRef.current = onChange
  const dataItemRef = React.useRef(dataItem)
  dataItemRef.current = dataItem
  const fieldRef = React.useRef(field)
  fieldRef.current = field

  React.useEffect(() => {
    return () => {
      let finalValue = valueRef.current;
      if (prefixRef.current || suffixRef.current) {
        finalValue = valueRef.current !== '' ? `${prefixRef.current}${valueRef.current}${suffixRef.current}` : '';
      }
      if (finalValue !== String(dataItemRef.current?.[fieldRef.current] || '')) {
        onChangeRef.current({ dataItem: dataItemRef.current, field: fieldRef.current, value: finalValue });
      }
    }
  }, [])

  const handleBlur = () => {
    let finalValue = localValue;
    if (prefix || suffix) {
      finalValue = localValue !== '' ? `${prefix}${localValue}${suffix}` : ''; 
    }
    if (finalValue !== String(dataItem?.[field] || '')) {
      onChange({ dataItem, field, value: finalValue });
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      inputRef.current?.blur();
    }
  };

  React.useEffect(() => {
    if (inputRef.current) {
      inputRef.current.focus();
    }
  }, []);

  return (
    <td style={{ padding: '0px' }}>
      <div style={{ display: 'flex', alignItems: 'center', width: '100%', height: '100%', padding: '0 8px', backgroundColor: '#fff', border: '1px solid #ccc' }}>
        {prefix && <span style={{ color: '#666', marginRight: '2px', whiteSpace: 'nowrap' }}>{prefix}</span>}
        <input
          ref={inputRef}
          type="number"
          value={localValue}
          onChange={handleChange}
          onBlur={handleBlur}
          onKeyDown={(e) => {
            if (['e', 'E', '+', '-'].includes(e.key)) {
              e.preventDefault();
            }
            handleKeyDown(e);
          }}
          style={{
            border: 'none',
            outline: 'none',
            width: '100%',
            height: '28px',
            background: 'transparent',
            textAlign: 'left',
          }}
        />
        {suffix && <span style={{ color: '#666', marginLeft: '2px', whiteSpace: 'nowrap' }}>{suffix}</span>}
      </div>
    </td>
  );
};

export const CheckboxCellEditor = ({ dataItem, field, onChange }) => {
  const checked = !!dataItem[field];
  const isCellDisabled = dataItem?.isEditable === false;
  return (
    <td style={{ textAlign: 'center', padding: '6px 2px' }}>
      <Checkbox
        checked={checked}
        disabled={isCellDisabled}
        onChange={(e) => {
          if (isCellDisabled) return;
          const newVal = typeof e.target.checked === 'boolean' ? e.target.checked : !checked;
          onChange({ dataItem, field, value: newVal });
        }}
        size='medium'
      />
    </td>
  );
};

export const DynamicRowCellEditor = (props) => {
  const { dataItem, field, onChange } = props
  const inputType = dataItem?.type
  const allowNegative = dataItem?.allowNegative || false
  const rawOptions = dataItem?.options || []

  // Convert string array to object array for SelectCellEditor
  const options = rawOptions.map((opt) => {
    if (typeof opt === 'string') {
      return { value: opt, label: opt }
    }
    return opt
  })
  switch (inputType) {
    case 'number':
    case 'numeric':
      return <NoSpinnerNumericEditor {...props} allowNegative={allowNegative} />

    case 'dropdown':
    case 'select':
      // Use simple DropDownList for string arrays
      if (rawOptions.length > 0 && typeof rawOptions[0] === 'string') {
        return (
          <td>
            <DropDownList
              value={dataItem[field]}
              onChange={(e) =>
                onChange({ dataItem, field, value: e.target.value })
              }
              data={rawOptions}
              style={{
                width: '100%',
                backgroundColor: 'lightGrey',
              }}
            />
          </td>
        )
      }
      return (
        <SelectCellEditor
          {...props}
          options={options}
          textField='label'
          valueField='value'
        />
      )

    case 'boolean':
    case 'yesno':
      return <BooleanCellEditor {...props} />

    case 'formula-text':
      if (/^[+/-]+\s*\d+(?:\.\d+)?\s*%?$/.test(String(dataItem?.[field] || ''))) {
        return <FormulaTextEditor {...props} />
      }
      return <NoSpinnerNumericEditor {...props} />

    case 'text':
      return <TextCellEditorUpdated {...props} />

    case 'date':
      return <DateOnlyPicker {...props} />

    case 'datetime':
      return <DateTimePickerEditor {...props} />

    case 'checkbox':
      return <CheckboxCellEditor {...props} />

    default:
      return <NoSpinnerNumericEditor {...props} allowNegative={allowNegative} />
  }
}

export const DynamicRowDisplayCell = (props) => {
  const { dataItem, field, tdProps, format } = props
  const value = dataItem?.[field]
  const inputType = dataItem?.type

  let displayValue = value

  if (inputType === 'boolean' || inputType === 'yesno' || inputType === 'checkbox') {
    displayValue = typeof value === 'boolean' ? (value ? 'Yes' : 'No') : value
  } else if (inputType === 'date' && value instanceof Date) {
    const year = value.getFullYear()
    const month = String(value.getMonth() + 1).padStart(2, '0')
    const day = String(value.getDate()).padStart(2, '0')
    displayValue = `${year}-${month}-${day}`
  } else if (inputType === 'datetime' && value instanceof Date) {
    const year = value.getFullYear()
    const month = String(value.getMonth() + 1).padStart(2, '0')
    const day = String(value.getDate()).padStart(2, '0')
    const hours = String(value.getHours()).padStart(2, '0')
    const minutes = String(value.getMinutes()).padStart(2, '0')
    displayValue = `${year}-${month}-${day} ${hours}:${minutes}`
  } else {
    // Format numbers using the format function from column definition
    if (!isNaN(value)) {
      displayValue = Number(value).toFixed(3)
    }
  }

  return (
    <td {...tdProps} title={String(displayValue ?? '')}>
      {displayValue ?? ''}
    </td>
  )
}
