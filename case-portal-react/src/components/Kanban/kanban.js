import './kanban.css';
import * as React from 'react';
import { extend, addClass } from '@syncfusion/ej2-base';
import { KanbanComponent, ColumnsDirective, ColumnDirective } from "@syncfusion/ej2-react-kanban";
import { updateSampleSection } from './sampleBase';
import { registerLicense } from '@syncfusion/ej2-base';

export const Kanban = ({ stages, cases, kanbanConfig }) => {
    registerLicense('Mgo+DSMBaFt/QHRqVVhkX1pGaV5EQmFJfFBmTGldd1RwcEU3HVdTRHRcQlxiTn5ackxhXnpYdH0=;Mgo+DSMBPh8sVXJ0S0J+XE9AdVRAQmJPYVF2R2BJdlR0fF9GZEwgOX1dQl9gSX9ScUVqXXZcd3FST2k=;ORg4AjUWIQA/Gnt2VVhkQlFaclxJX3xIeEx0RWFab1d6cF1MYFhBNQtUQF1hSn5Sd0JjUHtXcnNQT2NV;OTU1NzMxQDMyMzAyZTM0MmUzMFlmODlLOGFIOWs1N08yZG1UM0tsWXpEZ2tuSlMwWUhHa0hCRWtVY096Q3c9;OTU1NzMyQDMyMzAyZTM0MmUzMGJyVEVuRjVpWGN6dWlqUGpXQmtiNjlNUWpvNENaSlc3YlpITGtObWpyNlk9;NRAiBiAaIQQuGjN/V0Z+WE9EaFtBVmFWf1VpR2NbfE55flFOalxVVAciSV9jS31Td0RkWXZaeXZSQGVeWA==;OTU1NzM0QDMyMzAyZTM0MmUzMFR1bTBxUmxnN29TOWk2T2RKNzgvVVlKS3Y5eEpicENmYVFhZ1lwZERZdE09;OTU1NzM1QDMyMzAyZTM0MmUzMFVDcWpFNjFLWlJabWJ3eXB4d2xDbE5RUHd6N3p6R01KejdmY3RNNHFjc2c9;Mgo+DSMBMAY9C3t2VVhkQlFaclxJX3xIeEx0RWFab1d6cF1MYFhBNQtUQF1hSn5Sd0JjUHtXcnNSR2FV;OTU1NzM3QDMyMzAyZTM0MmUzMFI3OVFhekd0TG41L0pGbUYwTHZyUCtXVWYyRUtmbmQ1dU5BeDZaWjNGblU9;OTU1NzM4QDMyMzAyZTM0MmUzMFJ5V1FLTSthUllSYjUxc0hPQmIzTHpZNFdjaWlMYVZsdXNNbUtYMGorcnM9;OTU1NzM5QDMyMzAyZTM0MmUzMFR1bTBxUmxnN29TOWk2T2RKNzgvVVlKS3Y5eEpicENmYVFhZ1lwZERZdE09');

    React.useEffect(() => {
        updateSampleSection();
    }, []);

    let data = extend([], cases, null, true);

    function cardRendered(args) {
        let val = "Low";
        addClass([args.element], val);
    }

    function columnTemplate(props) {
        return (<div className="header-template-wrap">
            <div className={"header-icon e-icons " + "Open"}></div>
            <div className="header-text">{props.headerText}</div>
        </div>);
    }

    function cardTemplate(props) {
        // card css styling docs: https://ej2.syncfusion.com/angular/documentation/card/style

        let title = kanbanConfig?.title;
        let content = kanbanConfig?.content;

        return (<div className={"card-template"}>
            <div className="e-card-header">
                <div className="e-card-header-caption">
                    <div className="e-card-header-title">{props.businessKey}</div>

                    <div className="e-card-header-title">
                        {title && title.length > 0 ?
                            title.map(attributeName => { return props.attributes.find(o => o.name === attributeName)?.value }).join(" ")
                            : props.attributes.slice(0, 2).map(attribute => { return attribute.value; }).join(" ")
                        }
                    </div>
                </div>
            </div>
            <div className="e-card-content">
                <div className="e-text">
                    {content && content.length > 0 ?
                        content.map(attributeName => { return props.attributes.find(o => o.name === attributeName)?.value }).join(" ")
                        : props.attributes.slice(2, 3).map(attribute => { return attribute.value; }).join(" ")
                    }
                </div>
            </div>
            <div className="e-card-custom-footer">
                <div className="e-card-tag-field">{props.statusDescription}</div>
            </div>

        </div>);
    }

    function getString(assignee) {
        return assignee.match(/\b(\w)/g).join("").toUpperCase();
    }

    function DialogOpen(args) {
        args.cancel = true;
    }

    return (<div className='schedule-control-section'>
        <div className='col-lg-12 control-section'>
            <div className='control-wrapper'>
                <KanbanComponent id="kanban" cssClass="kanban-overview" keyField="stage" dataSource={data} enableTooltip={true} cardSettings={{ template: cardTemplate.bind(this) }} cardRendered={cardRendered.bind(this)} dialogOpen={DialogOpen.bind(this)}>
                    <ColumnsDirective>
                        {stages.map(stage => {
                            return <ColumnDirective headerText={stage.name} keyField={stage.name} allowToggle={true} allowDrag={false} allowDrop={false} template={columnTemplate.bind(this)} />;
                        })}
                    </ColumnsDirective>
                </KanbanComponent>
            </div>
        </div>
    </div>);
}