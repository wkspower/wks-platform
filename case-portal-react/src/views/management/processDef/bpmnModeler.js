import CloseIcon from '@mui/icons-material/Close';
import AppBar from '@mui/material/AppBar';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import IconButton from '@mui/material/IconButton';
import Slide from '@mui/material/Slide';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import React, { useCallback, useMemo, useRef, useState, useEffect } from 'react';
import { Box, Grid } from '@mui/material';
import { ProcessDefService } from 'services/ProcessDefService';

import {
    BpmnModeler as CamundaWebModeler,
    ContentSavedReason,
    CustomBpmnJsModeler,
    Event,
    isBpmnIoEvent,
    isContentSavedEvent,
    isNotificationEvent,
    isPropertiesPanelResizedEvent,
    isUIUpdateRequiredEvent
} from "@miragon/camunda-web-modeler";

const Transition = React.forwardRef(function Transition(props, ref) {
    return <Slide direction="up" ref={ref} {...props} />;
});

export const BPMNModeler = ({ open, keycloak, processDef, handleClose }) => {

    const [bpmnXml, setBpmnXml] = useState();

    useEffect(() => {
        ProcessDefService.getBPMNXml(keycloak, processDef.bpmEngineId, processDef.id)
            .then((data) => {
                setBpmnXml(data);
            })
            .catch((err) => {
                setBpmnXml(null);
                console.log(err.message);
            });
    }, [open]);

    const modelerRef = useRef();

    const onXmlChanged = useCallback((
        newXml,
        newSvg,
        reason
    ) => {
        console.log(`Model has been changed because of ${reason}`);
        // Do whatever you want here, save the XML and SVG in the backend etc.
        setBpmnXml(newXml);
    }, []);

    const onSaveClicked = useCallback(async () => {
        if (!modelerRef.current) {
            // Should actually never happen, but required for type safety
            return;
        }

        console.log("Saving model...");
        const result = await modelerRef.current.save();
        console.log("Saved model!", result.xml, result.svg);
    }, []);

    const onEvent = useCallback(async (event) => {
        if (isContentSavedEvent(event)) {
            // Content has been saved, e.g. because user edited the model or because he switched
            // from BPMN to XML.
            onXmlChanged(event.data.xml, event.data.svg, event.data.reason);
            return;
        }

        if (isNotificationEvent(event)) {
            // There's a notification the user is supposed to see, e.g. the model could not be
            // imported because it was invalid.
            return;
        }

        if (isUIUpdateRequiredEvent(event)) {
            // Something in the modeler has changed and the UI (e.g. menu) should be updated.
            // This happens when the user selects an element, for example.
            return;
        }

        if (isPropertiesPanelResizedEvent(event)) {
            // The user has resized the properties panel. You can save this value e.g. in local
            // storage to restore it on next load and pass it as initializing option.
            console.log(`Properties panel has been resized to ${event.data.width}`);
            return;
        }

        if (isBpmnIoEvent(event)) {
            // Just a regular bpmn-js event - actually lots of them
            return;
        }

        // eslint-disable-next-line no-console
        console.log("Unhandled event received", event);
    }, [onXmlChanged]);

    /**
     * ====
     * CAUTION: Using useMemo() is important to prevent additional render cycles!
     * ====
     */

    const xmlTabOptions = useMemo(() => ({
        className: undefined,
        disabled: undefined,
        monacoOptions: undefined
    }), []);

    const propertiesPanelOptions = useMemo(() => ({
        className: undefined,
        containerId: undefined,
        container: undefined,
        elementTemplates: undefined,
        hidden: undefined,
        size: {
            max: undefined,
            min: undefined,
            initial: undefined
        }
    }), []);

    const modelerOptions = useMemo(() => ({
        className: undefined,
        refs: [modelerRef],
        container: undefined,
        containerId: undefined,
        size: {
            max: undefined,
            min: undefined,
            initial: undefined
        }
    }), []);

    const bpmnJsOptions = useMemo(() => undefined, []);

    const modelerTabOptions = useMemo(() => ({
        className: undefined,
        disabled: undefined,
        bpmnJsOptions: bpmnJsOptions,
        modelerOptions: modelerOptions,
        propertiesPanelOptions: propertiesPanelOptions
    }), [bpmnJsOptions, modelerOptions, propertiesPanelOptions]);


    return (
        <div>
            <Dialog fullScreen open={open} onClose={handleClose} TransitionComponent={Transition}>
                <AppBar sx={{ position: 'relative' }}>
                    <Toolbar>
                        <IconButton
                            edge="start"
                            color="inherit"
                            aria-label="close"
                            onClick={handleClose}
                        >
                            <CloseIcon />
                        </IconButton>
                        <Typography sx={{ ml: 2, flex: 1 }} component="div">
                        </Typography>
                        <Button color="inherit">
                            Save
                        </Button>
                        <Button color="inherit">
                            Delete
                        </Button>
                    </Toolbar>
                </AppBar>

                <div style={{
                    height: "100vh"
                }}>
                    <CamundaWebModeler
                        xml={bpmnXml}
                        onEvent={onEvent}
                        xmlTabOptions={xmlTabOptions}
                        modelerTabOptions={modelerTabOptions} />
                </div>

            </Dialog>
        </div>
    );
};
