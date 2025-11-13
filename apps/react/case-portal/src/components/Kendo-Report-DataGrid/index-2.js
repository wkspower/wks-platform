import { GridColumn as Column, Grid } from '@progress/kendo-react-grid'
import '@progress/kendo-theme-default/dist/all.css'
import { Tooltip } from '../../../node_modules/@progress/kendo-react-tooltip/index'

const KendoDataGrid2 = ({ rows, columns, permissions }) => {
  const toolTipRenderer = (props) => {
    const value = props.dataItem[props.field]

    return (
      <td {...props.tdProps} title={value}>
        {props.children}
      </td>
    )
  }

  const SimpleHeaderWithTooltip = (props) => {
    const { ariaSort, ...restThProps } = props.thProps || {}

    return (
      <th
        {...restThProps}
        aria-sort={ariaSort}
        title={props.title}
        style={{ padding: '0px', borderRight: '1px solid #878787' }}
      >
        <Tooltip
          position='top'
          anchorElement='target'
          parentTitle={true}
          className='test'
        >
          {props.children}
        </Tooltip>
      </th>
    )
  }

  return (
    <div>
      <Tooltip openDelay={50} position='bottom' anchorElement='target'>
        <Grid
          style={{
            flex: 1,
            overflow: 'auto',
            ...(permissions?.isHeight ? { height: 500 } : {}),
          }}
          data={rows}
          dataItemKey='id'
          defaultSkip={0}
          defaultTake={100}
          pageable={
            rows?.length > 100
              ? {
                  buttonCount: 4,
                  pageSizes: [10, 50, 100],
                }
              : false
          }
        >
          {columns?.map((col) => {
            const {
              field,
              title,
              width,
              cell,
              format,
              filterType = 'text',
              isRightAlligned,
              hidden,
              widthT,
              type,
            } = col

            return (
              <Column
                key={field}
                field={field}
                title={title}
                hidden={hidden}
                cells={{
                  data: (props) => toolTipRenderer(props),

                  headerCell: SimpleHeaderWithTooltip,
                }}
                format={format}
                width={widthT}
              />
            )
          })}
        </Grid>
      </Tooltip>
    </div>
  )
}
export default KendoDataGrid2
