import React, { useCallback, useEffect, useState } from 'react';
import BpmnJS from 'bpmn-js/dist/bpmn-navigated-viewer.production.min.js';
import './BpmnIo.css';
import { useSession } from 'SessionStoreContext';

export const ReactBpmn = ({ url, activities }) => {
    const [containerRef, setContainerRef] = useState(React.createRef);
    const keycloak = useSession();

    useEffect(() => {
        fetch(url, {
            headers: {
                Authorization: `Bearer ${keycloak.token}`
            }
        })
            .then((response) => response.text())
            .then((text) => {
                memoizedCallback(text);
            });
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [activities]);

    const memoizedCallback = useCallback(
        (text) => {
            setContainerRef(React.createRef);
            const container = containerRef.current;
            const bpmnViewer = new BpmnJS({ container });
            bpmnViewer.importXML(text).then(() => {
                const canvas = bpmnViewer.get('canvas');
                activities.forEach((activity) =>
                    canvas.addMarker(activity.activityId, 'highlight')
                );
                canvas.zoom('fit-viewport');
            });
        },
        // eslint-disable-next-line react-hooks/exhaustive-deps
        [activities]
    );

    const Div = useCallback(
        ({ containerRef }) => {
            return (
                <div
                    style={{
                        height: 500,
                        padding: '10px',
                        margin: '10px',
                        border: '1px solid rgba(0, 0, 0, 0.05)'
                    }}
                    className="react-bpmn-diagram-container"
                    ref={containerRef}
                ></div>
            );
        },
        // eslint-disable-next-line react-hooks/exhaustive-deps
        [activities]
    );

    return <Div containerRef={containerRef} />;
};
