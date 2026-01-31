import * as React from 'react'
import { Grid, GridColumn as Column } from '@progress/kendo-react-grid'
import { Label } from '@progress/kendo-react-labels'
import { Switch } from '@progress/kendo-react-inputs'

export const products = [
  {
    ProductID: 1,
    ProductName: 'Chai',
    SupplierID: 1,
    CategoryID: 1,
    QuantityPerUnit: '10 boxes x 20 bags',
    UnitPrice: 18.0,
    UnitsInStock: 39,
    UnitsOnOrder: 0,
    ReorderLevel: 10,
    Discontinued: false,
    Category: {
      CategoryID: 1,
      CategoryName: 'Beverages',
      Description: 'Soft drinks, coffees, teas, beers, and ales',
    },
  },
  {
    ProductID: 2,
    ProductName: 'Chang',
    SupplierID: 1,
    CategoryID: 1,
    QuantityPerUnit: '24 - 12 oz bottles',
    UnitPrice: 19.0,
    UnitsInStock: 17,
    UnitsOnOrder: 40,
    ReorderLevel: 25,
    Discontinued: false,
    Category: {
      CategoryID: 1,
      CategoryName: 'Beverages',
      Description: 'Soft drinks, coffees, teas, beers, and ales',
    },
  },
  {
    ProductID: 3,
    ProductName: 'Aniseed Syrup',
    SupplierID: 1,
    CategoryID: 2,
    QuantityPerUnit: '12 - 550 ml bottles',
    UnitPrice: 10.0,
    UnitsInStock: 13,
    UnitsOnOrder: 70,
    ReorderLevel: 25,
    Discontinued: false,
    Category: {
      CategoryID: 2,
      CategoryName: 'Condiments',
      Description: 'Sweet and savory sauces, relishes, spreads, and seasonings',
    },
  },
  {
    ProductID: 4,
    ProductName: "Chef Anton's Cajun Seasoning",
    SupplierID: 2,
    CategoryID: 2,
    QuantityPerUnit: '48 - 6 oz jars',
    UnitPrice: 22.0,
    UnitsInStock: 53,
    UnitsOnOrder: 0,
    ReorderLevel: 0,
    Discontinued: false,
    Category: {
      CategoryID: 2,
      CategoryName: 'Condiments',
      Description: 'Sweet and savory sauces, relishes, spreads, and seasonings',
    },
  },
  {
    ProductID: 5,
    ProductName: "Chef Anton's Gumbo Mix",
    SupplierID: 2,
    CategoryID: 2,
    QuantityPerUnit: '36 boxes',
    UnitPrice: 21.35,
    UnitsInStock: 0,
    UnitsOnOrder: 0,
    ReorderLevel: 0,
    Discontinued: true,
    Category: {
      CategoryID: 2,
      CategoryName: 'Condiments',
      Description: 'Sweet and savory sauces, relishes, spreads, and seasonings',
    },
  },
]

const StickyTable = () => {
  const [locked, setLocked] = React.useState(false)

  return (
    <div>
      <div className='mb-3'>
        <Label>
          Lock Additional details Column: &nbsp;
          <Switch onChange={() => setLocked(!locked)} checked={locked} />
        </Label>
      </div>
      <div style={{ width: '100%', overflow: 'auto' }}>
        <Grid
          style={{
            height: 350,
            width: '100%',
          }}
          data={products}
          reorderable={true}
          scrollable='virtual'
        >
          <Column field='ProductID' title='ID' width='80px' locked={true} />
          <Column
            field='ProductName'
            title='Name'
            width='200px'
            locked={true}
          />
          <Column
            field='Category.CategoryName'
            title='CategoryName'
            width='200px'
          />
          <Column field='UnitPrice' title='Price' width='100px' />
          <Column field='UnitsInStock' title='In stock' width='100px' />
          <Column field='UnitsOnOrder' title='On order' width='100px' />
          <Column field='Discontinued' title='Discontinued' width='120px' />
          <Column field='UnitPrice' title='Price' width='100px' />
          <Column field='UnitsInStock' title='In stock' width='100px' />
          <Column field='UnitsOnOrder' title='On order' width='100px' />
          <Column field='Discontinued' title='Discontinued' width='120px' />
          <Column field='UnitPrice' title='Price' width='100px' />
          <Column field='UnitsInStock' title='In stock' width='100px' />
          <Column field='UnitsOnOrder' title='On order' width='100px' />
          <Column field='Discontinued' title='Discontinued' width='120px' />
          <Column
            field='QuantityPerUnit'
            title='Additional details'
            width='250px'
            locked={locked}
          />
        </Grid>
      </div>
    </div>
  )
}

export default StickyTable
