import CloseIcon from '@mui/icons-material/Close'
import AppBar from '@mui/material/AppBar'
import Button from '@mui/material/Button'
import Dialog from '@mui/material/Dialog'
import IconButton from '@mui/material/IconButton'
import Slide from '@mui/material/Slide'
import Toolbar from '@mui/material/Toolbar'
import Typography from '@mui/material/Typography'
import React, { useCallback, useRef, useState, useEffect } from 'react'
import { ProcessDefService } from 'services/ProcessDefService'
import { DeploymentService } from 'services/DeploymentService'

import BpmnModeler from 'bpmn-js/lib/Modeler'
import {
  BpmnPropertiesPanelModule,
  BpmnPropertiesProviderModule,
  CamundaPlatformPropertiesProviderModule,
} from 'bpmn-js-properties-panel'
import camundaModdleDescriptor from 'camunda-bpmn-moddle/resources/camunda.json'

import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css'
import '@bpmn-io/properties-panel/dist/assets/properties-panel.css'

import newProcessXml from './new-process'

const PROPERTIES_PANEL_WIDTH = 340

const Transition = React.forwardRef(function Transition(props, ref) {
  return <Slide direction='up' ref={ref} {...props} />
})

export const BPMNModeler = ({ open, keycloak, processDef, handleClose }) => {
  const canvasRef = useRef(null)
  const panelRef = useRef(null)
  const modelerRef = useRef(null)
  const [bpmnXml, setBpmnXml] = useState(null)

  useEffect(() => {
    if (!open) return

    if (!processDef.id) {
      setBpmnXml(newProcessXml)
    } else {
      ProcessDefService.getBPMNXml(keycloak, processDef.id)
        .then((data) => setBpmnXml(data))
        .catch((err) => {
          setBpmnXml(null)
          console.log(err.message)
        })
    }
  }, [open, processDef.id, keycloak])

  useEffect(() => {
    if (!open || !canvasRef.current || !panelRef.current) return

    const modeler = new BpmnModeler({
      container: canvasRef.current,
      propertiesPanel: { parent: panelRef.current },
      additionalModules: [
        BpmnPropertiesPanelModule,
        BpmnPropertiesProviderModule,
        CamundaPlatformPropertiesProviderModule,
      ],
      moddleExtensions: { camunda: camundaModdleDescriptor },
    })

    modelerRef.current = modeler

    return () => {
      modeler.destroy()
      modelerRef.current = null
    }
  }, [open])

  useEffect(() => {
    const modeler = modelerRef.current
    if (!modeler || !bpmnXml) return

    modeler.importXML(bpmnXml).catch((err) => {
      console.log('Failed to import BPMN XML', err)
    })
  }, [bpmnXml])

  const onSaveClicked = useCallback(async () => {
    const modeler = modelerRef.current
    if (!modeler) return

    const { xml } = await modeler.saveXML({ format: true })
    DeploymentService.deploy(keycloak, xml).then(() => {
      handleClose()
    })
  }, [keycloak, handleClose])

  return (
    <div>
      <Dialog
        fullScreen
        open={open}
        onClose={handleClose}
        slots={{
          transition: Transition,
        }}
      >
        <div
          style={{
            display: 'flex',
            height: '100vh',
            width: '100%',
          }}
        >
          <div
            ref={canvasRef}
            style={{ flex: 1, height: '100%', overflow: 'hidden' }}
          />
          <div
            ref={panelRef}
            style={{
              width: PROPERTIES_PANEL_WIDTH,
              height: '100%',
              borderLeft: '1px solid rgba(0, 0, 0, 0.12)',
              overflow: 'auto',
            }}
          />
        </div>

        <AppBar
          sx={{ top: 'auto', left: 0, bottom: 0, width: '20%', zIndex: '1' }}
        >
          <Toolbar>
            <IconButton
              edge='start'
              color='inherit'
              aria-label='close'
              onClick={handleClose}
            >
              <CloseIcon />
            </IconButton>
            <Typography sx={{ ml: 2, flex: 1 }} component='div'>
              <div>{processDef.name}</div>
            </Typography>
            <Button color='inherit' onClick={onSaveClicked}>
              Save
            </Button>
          </Toolbar>
        </AppBar>
      </Dialog>
    </div>
  )
}
