import React, { useEffect, useRef } from 'react'
import BpmnViewer from 'bpmn-js'
import { mockProcess } from './mockProcess'

function ProcessViewer() {
  const viewerRef = useRef(null)

  useEffect(() => {
    const viewer = new BpmnViewer({
      container: viewerRef.current,
    })

    async function loadDiagram() {
      const xml = mockProcess.bpmn20Xml

      await viewer.importXML(xml)

      const canvas = viewer.get('canvas')
      canvas.zoom('fit-viewport')
    }

    loadDiagram()
  }, [])

  return (
    <div
      ref={viewerRef}
      style={{
        height: '500px',
        border: '1px solid #ccc',
      }}
    />
  )
}

export default ProcessViewer
