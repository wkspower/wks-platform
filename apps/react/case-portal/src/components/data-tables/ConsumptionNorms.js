import { DataService } from 'services/DataService'
import DataGridTable from './ASDataGrid'
import { useEffect, useState } from 'react'
import { useSession } from 'SessionStoreContext'
import { generateHeaderNames } from 'components/Utilities/generateHeaders'

const NormalOpNormsScreen = () => {
  const keycloak = useSession();
  const [csData, setCsData] = useState([]);
  const [csDataTransformed, setCsDataTransformed] = useState([]);
  const [allProducts, setAllProducts] = useState([]);
  const headerMap = generateHeaderNames();

  useEffect(() => {
    const fetchData = async () => {
      try {
        const data = await DataService.getConsumptionNormsData(keycloak);
        setCsData(data);

        let rowIndex = 1;
        const groupedRows = [];

        Object.entries(data).forEach(([Particulars, rows], index) => {
          groupedRows.push({
            id: `group-${index}`,
            Particulars: Particulars,
          });
          rows.forEach((row) => {
            groupedRows.push({
              ...row,
              id: row.NormParameterMonthlyTransactionId || `row-${rowIndex++}`,
            });
          });
        });

        setCsDataTransformed(groupedRows);
      } catch (error) {
        console.error("Error fetching data:", error);
      }
    };

    const getAllProducts = async () => {
      try {
        const data = await DataService.getAllProducts(keycloak);
        const productList = data.map((product) => ({
          id: product.id,
          displayName: product.displayName,
        }));
        setAllProducts(productList);
      } catch (error) {
        console.error("Error fetching products:", error);
      }
    };

    getAllProducts();
    fetchData();
  }, []);

  const productionColumns = [
    {
      field: "Particulars",
      headerName: "Particulars",
      minWidth: 150,
      editable: false,
      renderCell: (params) => {
        const isGroupRow = params.row.id.startsWith("group-");
        return (
          <span style={{ fontWeight: isGroupRow ? "bold" : "normal" }}>
            {params.value}
          </span>
        );
      },
    },

    
    
    { field: "TPH", headerName: "Unit", width: 100, editable: false },




    {
      field: 'NormParametersId',
      headerName: 'Product Norm',
      editable: true,
      minWidth: 225,
      valueGetter: (params , params2) => {
        return params || ''; 
      },
      valueFormatter: (params) => {
        const product = allProducts.find((p) => p.id === params);
        return product ? product.displayName : '';
      },
      renderEditCell: (params , params2) => {
        const { id, value } = params; 
        return (
          <select
            value={value} 
            onChange={(event) => {
              params.api.setEditCellValue({
                id: params.id,
                field: 'NormParametersId',
                value: event.target.value, 
              });
            }}
            style={{
              width: '100%',
              padding: '5px',
              border: 'none',  
              outline: 'none', 
              background: 'transparent', 
            }}
          >
            {allProducts.map((product) => (
              <option key={product.id} value={product.id}>
                {product.displayName}
              </option>
            ))}
          </select>
        );
      },
    }, 

    { field: 'apr24', headerName: headerMap['apr'], editable: true },
    { field: 'may24', headerName: headerMap['may'], editable: true },
    { field: 'jun24', headerName: headerMap['jun'], editable: true },
    { field: 'jul24', headerName: headerMap['jul'], editable: true },
    { field: 'aug24', headerName: headerMap['aug'], editable: true },
    { field: 'sep24', headerName: headerMap['sep'], editable: true },
    { field: 'oct24', headerName: headerMap['oct'], editable: true },
    { field: 'nov24', headerName: headerMap['nov'], editable: true },
    { field: 'dec24', headerName: headerMap['dec'], editable: true },
    { field: 'jan25', headerName: headerMap['jan'], editable: true },
    { field: 'feb25', headerName: headerMap['feb'], editable: true },
    { field: 'mar25', headerName: headerMap['mar'], editable: true },



    { field: "remark", headerName: "Remark",  editable: true },
  ];


  return (
    <div>
      <DataGridTable
        columns={productionColumns}
        rows={csDataTransformed}
        getRowId={(row) => row.id}
        title="Consumption AOP"
        paginationOptions={[100, 200, 300]}
        permissions={{
          showAction: true,
          addButton: false,
          deleteButton: false,
          editButton: true,
          showUnit: true,
          saveWithRemark: true,
        }}
      />
    </div>
  );
};


export default NormalOpNormsScreen
