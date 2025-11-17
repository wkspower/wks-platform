export const MockReportService = {
  async getReport({
    category,
    AOP_YEAR,
    verticalName,
    FORMAT_VALUES_3_DECIMAL,
    FORMAT_VALUES_2_DECIMAL,
    FORMAT_VALUES_COST,
    FORMAT_VALUES_PRICE,
  }) {
    const currFY = AOP_YEAR || ''

    let prevFY = ''
    if (currFY.includes('-')) {
      const [start, end] = currFY.split('-').map(Number)
      prevFY = `${start - 1}-${(end - 1).toString().padStart(2, '0')}`
    }
    switch (category) {
      // ==== A. Product mix and production ====
      case 'ProductMixAndProduction':
        return {
          columns: [
            {
              field: 'SrNo',
              title: 'SL.No',
              editable: false,
              widthT: 100,
              align: 'right',
              format: '{0:#.#}',
            },
            {
              field: 'ByProductName',
              title: 'Product name',
              editable: false,
              widthT: 300,
            },
            {
              field: 'Unit',
              title: verticalName === 'meg' ? 'UOM' : 'Unit',
              editable: false,
              widthT: 100,
            },
            {
              title: 'Price',
              children: [
                {
                  field: 'Price',
                  title: verticalName === 'meg' ? 'Rs/UOM' : 'Rs/MT',
                  editable: false,
                  align: 'right',
                  width: 120,
                  format: FORMAT_VALUES_PRICE,
                  type: 'number',
                },
              ],
            },
            {
              title: 'Production, MT',
              children: [
                {
                  title: prevFY,
                  children: [
                    {
                      field: 'PrevYearNormBudget',
                      title: 'Budget',
                      editable: false,
                      align: 'right',
                      width: 120,
                      format: FORMAT_VALUES_2_DECIMAL,
                      type: 'number',
                    },
                    {
                      field: 'PrevYearNormActual',
                      title: 'Actual',
                      editable: false,
                      align: 'right',
                      width: 120,
                      format: FORMAT_VALUES_2_DECIMAL,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: currFY,
                  children: [
                    {
                      field: 'NextYearCostBudget',
                      title: 'Budget',
                      editable: false,
                      align: 'right',
                      width: 120,
                      format: FORMAT_VALUES_2_DECIMAL,
                      type: 'number',
                    },
                  ],
                },
              ],
            },
          ],
        }

      // ==== B. By products ====
      case 'ByProducts':
        return {
          columns: [
            {
              field: 'SrNo',
              title: 'SL.No',
              widthT: 100,
              format: '{0:#.#}',
              editable: false,
              align: 'right',
            },
            {
              field: 'ByProductName',
              title: 'By product name',
              editable: false,
              widthT: 300,
            },
            {
              field: 'Unit',
              title: verticalName === 'meg' ? 'UOM' : 'Unit',
              editable: false,
              widthT: 100,
            },
            {
              title: 'Price',
              children: [
                {
                  field: 'Price',
                  title: verticalName === 'meg' ? 'Rs/UOM' : 'Rs/MT',
                  editable: false,
                  width: 120,
                  align: 'right',
                  format: FORMAT_VALUES_PRICE,

                  type: 'number',
                },
              ],
            },
            {
              title: 'Norm Unit/MT',
              children: [
                {
                  title: prevFY,
                  children: [
                    {
                      field: 'PrevYearNormBudget',
                      title: 'Budget',
                      editable: false,
                      width: 120,
                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,

                      type: 'number',
                    },
                    {
                      field: 'PrevYearNormActual',
                      title: 'Actual',
                      editable: false,
                      width: 120,
                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,

                      type: 'number',
                    },
                  ],
                },
                {
                  title: currFY,
                  children: [
                    {
                      field: 'NextYearNormActual',
                      title: 'Budget',
                      editable: false,
                      width: 120,
                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,

                      type: 'number',
                    },
                  ],
                },
              ],
            },
            {
              title: 'Cost Rs/MT',
              children: [
                {
                  title: prevFY,
                  children: [
                    {
                      field: 'PrevYearCostBudget',
                      title: 'Budget',
                      editable: false,
                      width: 120,
                      align: 'right',
                      format: FORMAT_VALUES_COST,

                      type: 'number',
                    },
                    {
                      field: 'PrevYearCostActual',
                      title: 'Actual',
                      editable: false,
                      width: 120,
                      align: 'right',
                      format: FORMAT_VALUES_COST,

                      type: 'number',
                    },
                  ],
                },
                {
                  title: currFY,
                  children: [
                    {
                      field: 'NextYearCostActual',
                      title: 'Budget',
                      editable: false,
                      width: 120,
                      align: 'right',
                      format: FORMAT_VALUES_COST,

                      type: 'number',
                    },
                  ],
                },
              ],
            },
          ],
        }

      // ==== C. Raw material ====
      case 'RawMaterial':
        return {
          columns: [
            {
              field: 'SrNo',
              title: 'SL.No',
              widthT: 100,
              format: '{0:#.#}',
              editable: false,
              align: 'right',
            },
            {
              field: 'ByProductName',
              title: 'Raw material name',
              editable: false,
              widthT: 300,
            },
            {
              field: 'Unit',
              title: verticalName === 'meg' ? 'UOM' : 'Unit',
              editable: false,
              widthT: 100,
            },
            {
              title: 'Price',
              children: [
                {
                  field: 'Price',
                  title: verticalName === 'meg' ? 'Rs/UOM' : 'Rs/MT',
                  editable: false,
                  width: 120,
                  align: 'right',
                  format: FORMAT_VALUES_PRICE,

                  type: 'number',
                },
              ],
            },
            {
              title: 'Norm Unit/MT',
              children: [
                {
                  title: prevFY,
                  children: [
                    {
                      field: 'PrevYearNormBudget',
                      title: 'Budget',
                      editable: false,
                      width: 120,
                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,

                      type: 'number',
                    },
                    {
                      field: 'PrevYearNormActual',
                      title: 'Actual',
                      editable: false,
                      width: 120,
                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,

                      type: 'number',
                    },
                  ],
                },
                {
                  title: currFY,
                  children: [
                    {
                      field: 'NextYearNormActual',
                      title: 'Budget',
                      editable: false,
                      width: 120,
                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,

                      type: 'number',
                    },
                  ],
                },
              ],
            },
            {
              title: 'Cost Rs/MT',
              children: [
                {
                  title: prevFY,
                  children: [
                    {
                      field: 'PrevYearCostBudget',
                      title: 'Budget',
                      editable: false,
                      width: 120,
                      align: 'right',
                      format: FORMAT_VALUES_COST,

                      type: 'number',
                    },
                    {
                      field: 'PrevYearCostActual',
                      title: 'Actual',
                      editable: false,
                      width: 120,
                      align: 'right',
                      format: FORMAT_VALUES_COST,

                      type: 'number',
                    },
                  ],
                },
                {
                  title: currFY,
                  children: [
                    {
                      field: 'NextYearCostActual',
                      title: 'Budget',
                      editable: false,
                      width: 120,
                      align: 'right',
                      format: FORMAT_VALUES_COST,

                      type: 'number',
                    },
                  ],
                },
              ],
            },
          ],
        }

      // ==== D. Cat chem ====
      case 'CatChem':
        return {
          columns: [
            {
              field: 'SrNo',
              title: 'SL.No',
              widthT: 100,
              format: '{0:#.#}',
              editable: false,
              align: 'right',
            },
            {
              field: 'ByProductName',
              title: 'Catalyst name',
              editable: false,
              widthT: 300,
            },
            {
              field: 'Unit',
              title: verticalName === 'meg' ? 'UOM' : 'Unit',
              editable: false,
              widthT: 100,
            },
            {
              title: 'Price',
              children: [
                {
                  field: 'Price',
                  title: verticalName === 'meg' ? 'Rs/UOM' : 'Rs/MT',
                  editable: false,
                  width: 120,
                  align: 'right',
                  format: FORMAT_VALUES_PRICE,

                  type: 'number',
                },
              ],
            },
            {
              title: 'Norm Unit/MT',
              children: [
                {
                  title: prevFY,
                  children: [
                    {
                      field: 'PrevYearNormBudget',
                      title: 'Budget',
                      editable: false,
                      width: 120,
                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,

                      type: 'number',
                    },
                    {
                      field: 'PrevYearNormActual',
                      title: 'Actual',
                      editable: false,
                      width: 120,
                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,
                      type: 'number',
                    },
                  ],
                },
                {
                  title: currFY,
                  children: [
                    {
                      field: 'NextYearNormActual',
                      title: 'Budget',
                      editable: false,
                      width: 120,
                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,

                      type: 'number',
                    },
                  ],
                },
              ],
            },
            {
              title: 'Cost Rs/MT',
              children: [
                {
                  title: prevFY,
                  children: [
                    {
                      field: 'PrevYearCostBudget',
                      title: 'Budget',
                      editable: false,
                      width: 120,
                      align: 'right',
                      format: FORMAT_VALUES_COST,

                      type: 'number',
                    },
                    {
                      field: 'PrevYearCostActual',
                      title: 'Actual',
                      editable: false,
                      width: 120,
                      align: 'right',
                      format: FORMAT_VALUES_COST,

                      type: 'number',
                    },
                  ],
                },
                {
                  title: currFY,
                  children: [
                    {
                      field: 'NextYearCostActual',
                      title: 'Budget',
                      editable: false,
                      width: 120,
                      align: 'right',
                      format: FORMAT_VALUES_COST,

                      type: 'number',
                    },
                  ],
                },
              ],
            },
          ],
        }

      // ==== E. Utilities ====
      case 'Utilities':
        return {
          columns: [
            {
              field: 'SrNo',
              title: 'SL.No',
              widthT: 100,
              format: '{0:#.#}',
              editable: false,
              align: 'right',
            },
            {
              field: 'ByProductName',
              title: 'Utility name',
              editable: false,
              widthT: 300,
            },
            {
              field: 'Unit',
              title: verticalName === 'meg' ? 'UOM' : 'Unit',
              editable: false,
              widthT: 100,
            },
            {
              title: 'Price',
              children: [
                {
                  field: 'Price',
                  title: verticalName === 'meg' ? 'Rs/UOM' : 'Rs/MT',
                  editable: false,
                  width: 120,
                  align: 'right',
                  format: FORMAT_VALUES_PRICE,

                  type: 'number',
                },
              ],
            },
            {
              title: 'Norm Unit/MT',
              children: [
                {
                  title: prevFY,
                  children: [
                    {
                      field: 'PrevYearNormBudget',
                      title: 'Budget',
                      editable: false,
                      width: 120,
                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,

                      type: 'number',
                    },
                    {
                      field: 'PrevYearNormActual',
                      title: 'Actual',
                      editable: false,
                      width: 120,
                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,

                      type: 'number',
                    },
                  ],
                },
                {
                  title: currFY,
                  children: [
                    {
                      field: 'NextYearNormActual',
                      title: 'Budget',
                      editable: false,
                      width: 120,
                      align: 'right',
                      format: FORMAT_VALUES_3_DECIMAL,

                      type: 'number',
                    },
                  ],
                },
              ],
            },
            {
              title: 'Cost Rs/MT',
              children: [
                {
                  title: prevFY,
                  children: [
                    {
                      field: 'PrevYearCostBudget',
                      title: 'Budget',
                      editable: false,
                      width: 120,
                      align: 'right',
                      format: FORMAT_VALUES_COST,

                      type: 'number',
                    },
                    {
                      field: 'PrevYearCostActual',
                      title: 'Actual',
                      editable: false,
                      width: 120,
                      align: 'right',
                      format: FORMAT_VALUES_COST,

                      type: 'number',
                    },
                  ],
                },
                {
                  title: currFY,
                  children: [
                    {
                      field: 'NextYearCostActual',
                      title: 'Budget',
                      editable: false,
                      width: 120,
                      align: 'right',
                      format: FORMAT_VALUES_COST,

                      type: 'number',
                    },
                  ],
                },
              ],
            },
          ],
        }

      // ==== F. Other Variable Cost ====
      case 'OtherVariableCost':
        return {
          columns: [
            {
              field: 'SrNo',
              title: 'S.No',
              widthT: 80,
              align: 'right',
              editable: false,
              format: '{0:#.#}',
            },
            {
              field: 'id',
              hidden: true,
            },
            {
              field: 'OtherCost',
              title: 'Other cost',
              width: 200,
              editable: false,
            },
            {
              field: 'Unit',
              title: verticalName === 'meg' ? 'UOM' : 'Unit',
              widthT: 100,
              editable: false,
            },
            {
              title: prevFY, // e.g., '2023-24'
              children: [
                {
                  field: 'PrevYearBudget',
                  title: 'Budget',
                  width: 120,
                  align: 'right',
                  editable: true,
                  format: FORMAT_VALUES_COST,
                  type: 'numberNonGrey',
                },
                {
                  field: 'PrevYearActual',
                  title: 'Actual',
                  width: 120,
                  align: 'right',
                  editable: true,
                  format: FORMAT_VALUES_COST,
                  type: 'numberNonGrey',
                },
              ],
            },
            {
              title: currFY, // e.g., '2024-25'
              children: [
                {
                  field: 'CurrentYearBudget',
                  title: 'Budget',
                  width: 120,
                  align: 'right',
                  editable: true,
                  format: FORMAT_VALUES_COST,
                  type: 'numberNonGrey',
                },
              ],
            },
          ],
        }

      // ==== G. Production Cost Calculations ====
      case 'ProductionCostCalculations':
        return {
          columns: [
            {
              field: 'SrNo',
              title: 'SL.No',
              widthT: 100,
              format: '{0:#.#}',
              align: 'right',
              editable: false,
            },
            {
              field: 'ProductionCostCalculations',
              title: 'Production cost calculations',
              width: 220,
              editable: false,
            },
            {
              title: prevFY, // e.g., '2023-24'
              children: [
                {
                  field: 'PrevYearBudget',
                  title: 'Budget',
                  width: 120,
                  align: 'right',
                  editable: false,
                  format: FORMAT_VALUES_COST,
                  type: 'number',
                },
                {
                  field: 'PrevYearActual',
                  title: 'Actual',
                  width: 120,
                  align: 'right',
                  editable: false,
                  format: FORMAT_VALUES_COST,
                  type: 'number',
                },
              ],
            },
            {
              title: currFY, // e.g., '2024-25'
              children: [
                {
                  field: 'NextYearBudget',
                  title: 'Budget',
                  width: 120,
                  align: 'right',
                  editable: false,
                  format: FORMAT_VALUES_COST,
                  type: 'number',
                },
              ],
            },
          ],
        }

      default:
        throw new Error(`Unknown category: ${category}`)
    }
  },
}
