import { useEffect, useState } from 'react'
import { TextField, Button, Box } from '@mui/material'
import { DataService } from 'services/DataService'
import { useSession } from 'SessionStoreContext'

const TextSubmitMUI = () => {
  const [text, setText] = useState('')
  const keycloak = useSession()
  const [showTextBox, setShowTextBox] = useState(false)
  const [showCreateCasebutton, setShowCreateCasebutton] = useState(false)
  const [taskId, setTaskId] = useState('')
  const caseData = {
    caseDefinitionId: 'aopv3',
    owner: {
      id: keycloak.subject || '',
      name: keycloak.idTokenParsed.name || '',
      email: keycloak.idTokenParsed.email || '',
      phone: keycloak.idTokenParsed.phone || '',
    },
    attributes: [
      { name: 'textField', value: '9', type: 'String' },
      { name: 'submit', value: false, type: 'String' },
      { name: 'submit1', value: false, type: 'String' },
    ],
  }

  useEffect(() => {
    console.log('in the case id')
    // showCreateCasebutton = false;
    getCaseId()
  }, [])

  const getCaseId = async () => {
    try {
      // console.log("keycloak",keycloak);
      const data = await DataService.getCaseId(keycloak)
      // console.log('API Response:', data)
      if (!data || data.length === 0) {
        // console.log('API Response:')
        setShowCreateCasebutton(true)
      } else {
        const taskList = await DataService.getTasksByBusinessKey(
          keycloak,
          data[0].caseId,
        )
        console.log('taskList', taskList)
        for (var i = 0; i < taskList.length; i++) {
          if (taskList[i].assignee === 'maintenance_head') {
            setShowTextBox(true)
            setTaskId(taskList[i].id)
          }
        }
      }
      // Assuming each product object has a "name" property
      // const products = data.map((item) => item.displayName || item.name || item)
      // setProductOptions(products)
    } catch (error) {
      console.error('Error fetching product:', error)
    }
  }

  const createCase = async () => {
    try {
      const result = await DataService.createCase(keycloak, caseData)
      console.log('Response:', result)

      var year = localStorage.getItem('year')
      var plantId = ''
      var siteId = ''
      const storedPlant = localStorage.getItem('selectedPlant')
      if (storedPlant) {
        const parsedPlant = JSON.parse(storedPlant)
        plantId = parsedPlant.id
      }

      const storedSite = localStorage.getItem('selectedSite')
      if (storedSite) {
        const parsedSite = JSON.parse(storedSite)
        siteId = parsedSite.id
      }

      const verticalId = localStorage.getItem('verticalId')

      let workflowData = {
        year: year,
        plantFkId: plantId,
        caseDefId: caseData.caseDefinitionId,
        caseId: result.businessKey,
        siteFKId: siteId,
        verticalFKId: verticalId,
      }

      const workFlowResult = await DataService.saveworkflow(
        workflowData,
        keycloak,
      )
      console.log(workFlowResult)

      // alert('Submitted successfully!')
    } catch (error) {
      console.error('Error submitting:', error)
      // alert('Something went wrong!')
    }
  }
  const handleSubmit = async () => {
    try {
      if (!text.trim()) {
        console.log('Please enter a message!')
        return
      }
      const result = await DataService.completeTask(
        keycloak,
        taskId,
        caseData.attributes,
      )

      //const result = await DataService.saveText(text)
      console.log('Response:', result)
      // alert('Submitted successfully!')
    } catch (error) {
      console.error('Error submitting:', error)
      // alert('Something went wrong!')
    }
  }

  return (
    <div>
      <Button
        variant='contained'
        color='primary'
        onClick={createCase}
        sx={{ mt: 2 }}
        disabled={!showCreateCasebutton}
      >
        Create case
      </Button>
      {showTextBox && (
        <Box sx={{ maxWidth: 500, mx: 'auto', mt: 4, p: 2 }}>
          <TextField
            label='Enter your message'
            multiline
            rows={5}
            fullWidth
            variant='outlined'
            value={text}
            onChange={(e) => setText(e.target.value)}
          />
          <Button
            variant='contained'
            color='primary'
            onClick={handleSubmit}
            sx={{ mt: 2 }}
          >
            Submit
          </Button>
        </Box>
      )}
    </div>
  )
}

export default TextSubmitMUI
