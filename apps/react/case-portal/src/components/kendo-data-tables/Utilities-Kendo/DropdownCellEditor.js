import { DropDownList } from "@progress/kendo-react-dropdowns";
import { useMemo } from "react";

const DropdownCellEditor = (props) => {
  const { dataItem, field, onChange, options, ...tdProps } = props;

  // Convert simple string array to Kendo dropdown objects
  const allOptions = useMemo(
    () =>
      options.map((opt) => ({
        value: opt,
        label: opt,
      })),
    [options]
  );

  const currentValueObj = useMemo(
    () => allOptions.find((opt) => opt.value === dataItem[field]) || null,
    [allOptions, dataItem, field]
  );

  if (typeof onChange === "function") {
    const handleChange = (e) => {
      onChange({
        dataItem,
        field,
        value: e.value?.value,
      });
    };

    return (
      <DropDownList
        data={allOptions}
        textField="label"
        dataItemKey="value"
        value={currentValueObj}
        onChange={handleChange}
        style={{ width: "100%" }}
      />
    );
  }

  return (
    <td
      {...tdProps}
      style={{
        padding: "0.5rem 1rem",
        whiteSpace: "nowrap",
        overflow: "hidden",
        textOverflow: "ellipsis",
      }}
    >
      {dataItem[field] || "—"}
    </td>
  );
};

export default DropdownCellEditor;
