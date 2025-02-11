import React, { useEffect, useState } from 'react'
import { FormBuilder } from '@formio/react'
import { RecordService } from 'services'
import { StorageService } from 'plugins/storage'

function WKSFormBuilder({ form = {}, keycloak = {} }) {
  const [builder, setBuilder] = useState(null)
  const [builderKey, setBuilderKey] = useState(0)

  useEffect(() => {
    const fetchData = async () => {
      await buildDynamicRecordTypesBeforeCreateForm()
    }
    fetchData()
  }, [form])

  const buildDynamicRecordTypesBeforeCreateForm = async () => {
    const records = await RecordService.getAllRecordTypes(keycloak)
    const components = {}

    function addComponent(record) {
      let labels = records.reduce((acc, record) => {
        let labelData = record.fields?.components?.find(
          (value) => value.properties?.defaultLabel === 'true',
        )

        acc[record.id] = labelData || {
          label: record.id,
          valueProperty: record.id,
        }

        return acc
      }, { })



      components[record.id] = {
        title: record.id,
        key: record.id,
        icon: 'terminal',
        schema: {
          label: record.id,
          type: 'selectboxes',
          key: record.id,
          input: true,
          dataSrc: 'url',
          valueProperty: labels[record.id]?.key || '',
          template: `<span>{{ item.${labels[record.id]?.key || 'id'} }}</span>`,
          properties: {
            _internalClass_: 'recordType',
          },
        },
      }
    }

    records.map((entry) => {
      addComponent(entry)
    })

    const builder = {
      custom: {
        title: 'Record Types',
        weight: 10,
        components: components,
      },
    }

    setBuilder((prevBuilder) => ({
      ...prevBuilder,
      custom: {
        title: 'Record Types',
        weight: 10,
        components: components,
      },
    }))

    setBuilderKey((prevKey) => prevKey + 1)
  }

  return (
    <>
      <FormBuilder
        key={builderKey}
        form={form.structure}
        options={{
          noNewEdit: true,
          noDefaultSubmitButton: true,
          fileService: new StorageService(),
          builder: builder,
        }}
      />
    </>
  )
}

export default WKSFormBuilder
