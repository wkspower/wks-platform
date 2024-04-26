import './kanban.css'
import { extend, addClass } from '@syncfusion/ej2-base'
import {
  KanbanComponent,
  ColumnsDirective,
  ColumnDirective,
} from '@syncfusion/ej2-react-kanban'
import { registerLicense } from '@syncfusion/ej2-base'
import Link from '@mui/material/Link'

export const Kanban = ({
  stages,
  cases,
  caseDefId,
  kanbanConfig,
  setACase,
  setOpenCaseForm,
}) => {
  registerLicense(
    'ORg4AjUWIQA/Gnt2VVhkQlFadVdJXGFWfVJpTGpQdk5xdV9DaVZUTWY/P1ZhSXxQdkdiX39adXNWRGZYVkw=',
  )

  let data = extend([], cases, null, true)

  function cardRendered(args) {
    let val = 'Low'
    addClass([args.element], val)
  }

  function columnTemplate(props) {
    return (
      <div className='header-template-wrap'>
        <div className={'header-icon e-icons Open'}></div>
        <div className='header-text'>{props.headerText}</div>
      </div>
    )
  }

  function cardTemplate(props) {
    // card css styling docs: https://ej2.syncfusion.com/angular/documentation/card/style

    let title = kanbanConfig?.title
    let content = kanbanConfig?.content

    return (
      <div className={'card-template'}>
        <div className='e-card-header'>
          <div className='e-card-header-caption'>
            <div className='e-card-header-title'>
              <Link
                component='button'
                variant='body2'
                onClick={(e) => {
                  setACase({
                    businessKey: props.businessKey,
                    caseDefinitionId: caseDefId,
                  })
                  e.stopPropagation()
                  setOpenCaseForm(true)
                }}
              >
                {props.businessKey}
              </Link>
            </div>

            <div className='e-card-header-title'>
              {title
                ?.map((attributeName) => {
                  return props.attributes.find((o) => o.name === attributeName)
                    ?.value
                })
                .join(' ')}
            </div>
          </div>
        </div>
        <div className='e-card-content'>
          <div className='e-text'>
            {content
              ?.map((attributeName) => {
                return props.attributes.find((o) => o.name === attributeName)
                  ?.value
              })
              .join(' ')}
          </div>
        </div>
        <div className='e-card-custom-footer'>
          <div className='e-card-tag-field e-tooltip-text'>
            {props.statusDescription}
          </div>
        </div>
      </div>
    )
  }

  function DialogOpen(args) {
    args.cancel = true
  }

  return (
    <div className='schedule-control-section'>
      <div className='col-lg-12 control-section'>
        <div className='control-wrapper'>
          <KanbanComponent
            id='kanban'
            cssClass='kanban-overview'
            keyField='stage'
            dataSource={data}
            enableTooltip={true}
            cardSettings={{ template: cardTemplate.bind(this) }}
            cardRendered={cardRendered.bind(this)}
            dialogOpen={DialogOpen.bind(this)}
          >
            <ColumnsDirective>
              {stages.map((stage) => {
                return (
                  <ColumnDirective
                    key={stage.id}
                    headerText={stage.name}
                    keyField={stage.name}
                    allowToggle={true}
                    allowDrag={false}
                    allowDrop={false}
                    template={columnTemplate.bind(this)}
                  />
                )
              })}
            </ColumnsDirective>
          </KanbanComponent>
        </div>
      </div>
    </div>
  )
}
