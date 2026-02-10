import React, { useMemo } from 'react'
import { Box, Button, Typography } from '@mui/material'
import { DatePicker } from '@progress/kendo-react-dateinputs'
import { TextArea } from '@progress/kendo-react-inputs'
import {
  CustomAccordion,
  CustomAccordionDetails,
  CustomAccordionSummary,
} from 'utils/CustomAccrodian'

const ConfigurationAccordian = ({
  startDate,
  endDate,
  summary,
  configurationExecutionDetails,
  isOldYear,
  READ_ONLY,
  setStartDate,
  setEndDate,
  setSummary,
  setDateEdited,
  setSummaryEdited,
  handleOpenDialog,
}) => {
  const formatDateForText = (date, time = false) => {
    if (!date) return ''
    const parsedDate = new Date(date)
    if (isNaN(parsedDate)) return 'Invalid Date'
    const day = String(parsedDate.getDate()).padStart(2, '0')
    const month = String(parsedDate.getMonth() + 1).padStart(2, '0')
    const year = parsedDate.getFullYear()
    let formatted = `${day}-${month}-${year}`
    if (time) {
      let hours = parsedDate.getHours()
      const minutes = String(parsedDate.getMinutes()).padStart(2, '0')
      const ampm = hours >= 12 ? 'PM' : 'AM'
      hours = hours % 12
      hours = hours ? hours : 12
      const formattedTime = `${String(hours).padStart(2, '0')}:${minutes} ${ampm}`
      formatted += ` ${formattedTime}`
    }
    return formatted
  }

  const startDateConfig = configurationExecutionDetails.find(
    (item) => item.Name === 'StartDate',
  )

  const endDateConfig = configurationExecutionDetails.find(
    (item) => item.Name === 'EndDate',
  )

  const startDateFromConfig = new Date(startDateConfig?.AttributeValue)
  const endDateDateFromConfig = new Date(endDateConfig?.AttributeValue)

  const accordian = useMemo(() => {
    return (
      <Box sx={{ mb: '0px' }}>
        <CustomAccordion defaultExpanded disableGutters>
          <CustomAccordionSummary
            aria-controls='meg-grid-content'
            id='meg-grid-header'
          >
            <Typography className='accordian-title'>
              AOP Historical Period Basis
            </Typography>
          </CustomAccordionSummary>
          <CustomAccordionDetails>
            <Box
              sx={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'flex-end',
                mt: 0,
              }}
            >
              <Box
                sx={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 1,
                  marginTop: '5px',
                }}
              >
                {true && (
                  <Box
                    sx={{ display: 'flex', alignItems: 'flex-start', gap: 1 }}
                  >
                    {/* Start Date */}
                    <Box
                      sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}
                    >
                      <Typography
                        className='button-title'
                        sx={{ whiteSpace: 'nowrap' }}
                      >
                        Start Date
                      </Typography>
                      <DatePicker
                        id='start-date'
                        format='dd-MM-yyyy'
                        value={startDate}
                        onChange={(e) => {
                          setStartDate(e.value)
                          setDateEdited(true)
                        }}
                        style={{ height: '80px' }}
                        size='medium'
                        disabled={READ_ONLY}
                      />
                    </Box>

                    {/* End Date */}
                    <Box
                      sx={{ display: 'flex', flexDirection: 'column', gap: 0 }}
                    >
                      <Typography
                        className='button-title'
                        sx={{ whiteSpace: 'nowrap' }}
                      >
                        End Date
                      </Typography>
                      <DatePicker
                        id='end-date'
                        format='dd-MM-yyyy'
                        value={endDate}
                        onChange={(e) => {
                          setEndDate(e.value)
                          setDateEdited(true)
                        }}
                        style={{ height: '80px' }}
                        size='medium'
                        disabled={READ_ONLY}
                      />
                    </Box>

                    {/* Load Button */}
                    {!isOldYear && (
                      <Button
                        variant='contained'
                        onClick={handleOpenDialog}
                        className='btn-save'
                        sx={{ alignSelf: 'flex-end' }}
                        disabled={READ_ONLY}
                      >
                        Load
                      </Button>
                    )}
                  </Box>
                )}

                {configurationExecutionDetails[0]?.ModifiedOn && (
                  <Typography
                    className={
                      READ_ONLY ? 'summary-title-disabled' : 'summary-title'
                    }
                    sx={{
                      whiteSpace: 'normal',
                      alignSelf: 'flex-end',
                    }}
                  >
                    {`(Last refreshed data on: ${formatDateForText(configurationExecutionDetails[0]?.ModifiedOn, true)} for the period from ${formatDateForText(startDateFromConfig)} to ${formatDateForText(endDateDateFromConfig)})`}
                  </Typography>
                )}
              </Box>
            </Box>

            <Box
              sx={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'flex-start',
                gap: 0,
                mt: 1,
              }}
            >
              <Typography
                className='button-title'
                sx={{ whiteSpace: 'nowrap' }}
              >
                AOP Design Basis
              </Typography>

              <TextArea
                disabled={READ_ONLY}
                value={summary}
                rows={3}
                onChange={(e) => {
                  setSummary(e.target.value)
                  setSummaryEdited(true)
                }}
              />
            </Box>
          </CustomAccordionDetails>
        </CustomAccordion>
      </Box>
    )
  }, [
    startDate,
    endDate,
    summary,
    configurationExecutionDetails,
    isOldYear,
    READ_ONLY,
    setStartDate,
    setEndDate,
    setSummary,
    setDateEdited,
    setSummaryEdited,
    handleOpenDialog,
  ])

  return accordian
}

export default ConfigurationAccordian
