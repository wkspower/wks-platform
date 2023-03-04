function getAllByStatus(status, limit) {
    if (!status) {
        return Promise.resolve([]);
    }

    var url = `${process.env.REACT_APP_API_URL}/case/?status=${status}&limit=${limit}`;

    return fetch(url)
        .then((response) => response.json())
        .then(mapperToCase);
}

const CaseService = {
    getAllByStatus: getAllByStatus
};

export default CaseService;

function mapperToCase(data) {
    const toCase = data.map((element) => {
        const createdAt = element.attributes.find((attribute) => attribute.name === 'createdAt');
        element.createdAt = createdAt ? createdAt.value : '11/12/2022';
        element.statusDescription = element.status;
        return element;
    });

    return Promise.resolve(toCase);
}
