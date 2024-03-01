const express = require("express");
const config = require("../config");
const cors = require("cors");

const app = express();

app.get("/novu-app-id", (req, res) => {
  res.set("Access-Control-Allow-Origin", "*");
  res.send({ novuAppId: config.NovuAppId });
});

function startServer() {
  const port = config.Port;

  app.use(cors({ credentials: true }));

  app.listen(port, () => {
    console.log(`Server is running on port ${port}`);
  });
}

module.exports = {
  startServer,
};
