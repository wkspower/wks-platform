import React from 'react'
import { NoSpinnerNumericEditor } from './numbericColumns'
import { SelectCellEditor } from './SelectCellEditor'
import { BooleanCellEditor } from './BooleanCellEditor'
import { TextCellEditorUpdated } from './TextCellEditorUpdated'
import DateOnlyPicker from './DatePicker'
import DateTimePickerEditor from './DatePickeronSelectedYr'
import { DropDownList } from '@progress/kendo-react-dropdowns'
import { Checkbox } from '@mui/material'

const parseFormulaText = (value, dataItem, field) => {
  const str = String(value ?? '').trim();
  const match = str.match(/^([+/-]+\s*)(\d+(?:\.\d+)?)(\s*%?)?$/);
  if (match) {
    return {
      prefix: match[1] || '',
      num: match[2] || '',
      suffix: match[3] || '',
    };
  }
  if (field === 'range') {
    const hasPercent = dataItem?.uom === '%' || str.includes('%');
    return {
      prefix: '+/- ',
      num: str.replace(/[^\d.]/g, ''),
      suffix: hasPercent ? '%' : '',
    };
  }
  return {
    prefix: '',
    num: str,
    suffix: '',
  };
};

const formatFormulaText = (num, prefix, suffix) => {
  if (num === '' || num === null || num === undefined) {
    return '';
  }
  return `${prefix || ''}${num}${suffix || ''}`;
};

const FormulaTextEditor = (props) => {
  const { dataItem, field, onChange, tdProps } = props;
  const initialRawValue = dataItem?.[field] ?? '';

  // Synchronous initialization - parsed once on mount
  const parsed = React.useMemo(
    () => parseFormulaText(initialRawValue, dataItem, field),
    []
  );

  const [localValue, setLocalValue] = React.useState(parsed.num);
  const prefix = parsed.prefix;
  const suffix = parsed.suffix;

  const isFirstRender = React.useRef(true);
  const inputRef = React.useRef(null);
  const lastSyncedValueRef = React.useRef(String(initialRawValue ?? ''));

  const getFullValue = React.useCallback(
    (val) => {
      if (val === '' || val === null || val === undefined) {
        return '';
      }
      return formatFormulaText(val, prefix, suffix);
    },
    [prefix, suffix]
  );

  const syncChange = React.useCallback(
    (val) => {
      const fullValue = getFullValue(val);
      if (fullValue !== lastSyncedValueRef.current) {
        lastSyncedValueRef.current = fullValue;
        onChange({ dataItem, field, value: fullValue });
      }
    },
    [dataItem, field, onChange, getFullValue]
  );

  const localValueRef = React.useRef(localValue);
  localValueRef.current = localValue;
  const onChangeRef = React.useRef(onChange);
  onChangeRef.current = onChange;
  const dataItemRef = React.useRef(dataItem);
  dataItemRef.current = dataItem;
  const fieldRef = React.useRef(field);
  fieldRef.current = field;

  // Unmount cleanup: sync if value hasn't been synced yet
  React.useEffect(() => {
    return () => {
      const fullValue = getFullValue(localValueRef.current);
      if (fullValue !== lastSyncedValueRef.current) {
        lastSyncedValueRef.current = fullValue;
        onChangeRef.current({
          dataItem: dataItemRef.current,
          field: fieldRef.current,
          value: fullValue,
        });
      }
    };
  }, [getFullValue]);

  // Debounced sync to grid (300ms) - matching NoSpinnerNumericEditor pattern
  React.useEffect(() => {
    if (isFirstRender.current) {
      isFirstRender.current = false;
      return;
    }

    const handler = setTimeout(() => {
      syncChange(localValue);
    }, 300);

    return () => clearTimeout(handler);
  }, [localValue, syncChange]);

  const handleChange = (e) => {
    const val = e.target.value;
    if (val === '' || /^\d*\.?\d*$/.test(val)) {
      setLocalValue(val);
    }
  };

  const handleBlur = () => {
    syncChange(localValue);
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      syncChange(localValue);
      inputRef.current?.blur();
    }
  };

  React.useEffect(() => {
    if (inputRef.current) {
      inputRef.current.focus();
    }
  }, []);

  return (
    <td {...tdProps} style={{ padding: '0px', ...tdProps?.style }}>
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          width: '100%',
          height: '100%',
          padding: '0 8px',
          backgroundColor: '#fff',
          border: '1px solid #ccc',
        }}
      >
        {prefix && (
          <span style={{ color: '#666', marginRight: '2px', whiteSpace: 'nowrap' }}>
            {prefix}
          </span>
        )}
        <input
          ref={inputRef}
          type="number"
          step="any"
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
        {suffix && (
          <span style={{ color: '#666', marginLeft: '2px', whiteSpace: 'nowrap' }}>
            {suffix}
          </span>
        )}
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
