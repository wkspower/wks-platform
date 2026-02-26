import Config from 'consts/index'
import { Formio } from 'formiojs'
import MemoryTokenManager from 'plugins/MemoryTokenManager'

class RecordTypeChoice extends Formio.Components.components.field {
  static schema(...extend) {
    return super.schema(
      {
        type: 'recordtype',
        label: 'Record Type',
        customOptions: {
          inputType: '',
          recordType: '',
          template: '{{ item._id }}',
        },
      },
      ...extend,
    )
  }

  static get builderInfo() {
    return {
      title: 'Record Type',
      group: 'basic',
      icon: 'code',
      weight: 2,
      schema: RecordTypeChoice.schema(),
    }
  }

  static editForm(...extend) {
    const editForm = super.editForm(...extend)

    editForm.components = editForm.components.map((tab) => {
      if (tab?.components?.length) {
        tab.components.forEach((element) => {
          if (element.key === 'data') {
            element.components.splice(0, 0, {
              type: 'input',
              key: 'customOptions.template',
              label: 'Template',
              placeholder: '',
              weight: 50,
            })

            element.components.splice(0, 0, {
              type: 'input',
              key: 'customOptions.valueProperty',
              label: 'Value Property',
              placeholder: '',
              weight: 50,
            })

            element.components.splice(0, 0, {
              type: 'select',
              key: 'customOptions.recordType',
              label: 'Choice Record Types',
              placeholder: '',
              weight: 50,
              dataSrc: 'url',
              template: `<span>{{item.id}}</span>`,
              data: {
                url: `${Config.CaseEngineUrl}/record-type`,
                headers: [
                  {
                    key: 'Authorization',
                    value: `Bearer ${MemoryTokenManager.getToken()}`,
                  },
                ],
              },
            })

            element.components.splice(0, 0, {
              type: 'select',
              key: 'customOptions.inputType',
              label: 'Render Type',
              placeholder: '',
              weight: 50,
              dataSrc: 'values',
              data: {
                values: [
                  { label: 'Select One', value: 'selectone' },
                  { label: 'Select Many', value: 'selectmany' },
                ],
              },
            })
          }
        })
      }

      return tab
    })

    return editForm
  }
}

Formio.Components.addComponent('recordtype', RecordTypeChoice)

export default RecordTypeChoice
