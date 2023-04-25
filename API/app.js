// Codes d’état HTTP
// https://learn.microsoft.com/fr-fr/partner-center/developer/error-codes

const express = require("express");
const bodyParser = require("body-parser");
const app = express();
const axios = require("axios");
const jwt = require("jsonwebtoken");
const CryptoJS = require("crypto-js");

const {
  MongoClient,
  ServerApiVersion,
  MongoBatchReExecutionError,
} = require("mongodb");
const uri = require("./credentials.json").mongoDB;
const client = new MongoClient(uri, {
  useNewUrlParser: true,
  useUnifiedTopology: true,
  serverApi: ServerApiVersion.v1,
});
const databaseName = "deeplearning";

const SECRET = require("./credentials.json").SECRET;
//const request = require('request');
const cors = require("cors");

// Extended: https://swagger.io/specification/#infoObject
const options = {
  swaggerDefinition: {
    info: {
      title: "DeepLearning",
      servers: ["http://localhost:3000"],
    },
  },
  apis: ["app.js"],
};

const swaggerUi = require("swagger-ui-express");
const swaggerDocument = require("./swagger.json");

app.use("/swagger", swaggerUi.serve, swaggerUi.setup(swaggerDocument, options));
app.use(cors());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

// Création de compte
app.post("/signup", async (req, res) => {
  const headers = JSON.parse(JSON.stringify(req.headers));
  if (headers.username !== undefined && headers.password !== undefined) {
    const username = headers.username;
    const password = headers.password;
    if (password.length >= 3) {
      var hashedPassword = hashPassword(password);
    }
    else {
      var hashedPassword = "";
    }
    if (username.length >= 3 && password.length >= 3) {
      code_retour = await createUser(username, hashedPassword);
    } else {
      code_retour = "400"; // Bad Request format incorrect
    }
  } else {
    code_retour = "400"; // Bad Request paramètre manquant
  }
  return res.send(code_retour);
});

async function createUser(username, password) {
  try {
    var userExist = await getUser(username);
    if (userExist === null) {
      var liste = [];
      const database = client.db(databaseName);
      const users = database.collection("users");
      const query = { username: username, password: password, liste: liste };
      const user = await users.insertOne(query);
      return "200"; // Utilisateur créé
    } else {
      return "409"; // L'utilisateur existe déjà
    }
  } catch (error) {
    return "500"; // Erreur du serveur
  }
}

// Connexion
app.get("/signin", async (req, res) => {
  const headers = JSON.parse(JSON.stringify(req.headers));
  const username = headers.username;
  const password = hashPassword(headers.password);
  const token = await authentification(username, password);
  if (token) {
    code_retour = token;
  } else {
    code_retour = "404";
  }
  return res.send(code_retour);
});

async function authentification(username, password) {
  var user = await getUser(username);
  if (user === null) {
    return "404";
  }
  if (user.username == username && user.password == password) {
    const token = jwt.sign(
      {
        id: user.id,
        username: user.username,
      },
      SECRET,
      { expiresIn: "1h" }
    );
    return token;
  } else {
    return "404";
  }
}

// Récupérer les informations de l'utilisateur
app.get("/getInfo", async (req, res) => {
  const headers = JSON.parse(JSON.stringify(req.headers));
  const token = headers.token;
  const user = await checkToken(token);
  if (user) {
    message_retour = await getUser(user.username);
  } else {
    message_retour = "403";
  }
  return res.send(message_retour);
});

async function getUser(username) {
  try {
    const database = client.db(databaseName);
    const users = database.collection("users");
    const query = { username: username };
    const user = await users.findOne(query);
    return user;
  } catch (error) {
    return "503";
  }
}

// Ajouter un mot
app.put("/addWord", async (req, res) => {
  const headers = JSON.parse(JSON.stringify(req.headers));
  const token = headers.token;
  const user = await checkToken(token);
  if (user) {
    code_retour = await addWord(user.username, req.query.word);
  } else {
    code_retour = "403";
  }
  return res.send(code_retour);
});

async function addWord(username, word) {
  if (word === undefined || word.length === 0) {
    return "400";
  }
  try {
    const database = client.db(databaseName);
    const users = database.collection("users");
    const userItem = await getUser(username);

    var exist = false;
    for (let i = 0; i < userItem.liste.length; i++) {
      if (Object.keys(userItem.liste[i])[0] == word) {
        exist = true;
      }
    }

    if (!exist) {
      const wordElement = {
        [word]: "",
      };

      const response = await users.updateOne(
        { username: username },
        { $push: { liste: wordElement } }
      );
      return "200";
    } else {
      return "409";
    }
  } catch (error) {
    return "503";
  }
}

// Supprimer un mot
app.put("/delWord", async (req, res) => {
  const headers = JSON.parse(JSON.stringify(req.headers));
  const token = headers.token;
  const user = await checkToken(token);
  if (user) {
    code_retour = await delWord(user.username, req.query.word);
  } else {
    code_retour = "403";
  }
  return res.send(code_retour);
});

async function delWord(username, word) {
  if (word === undefined || word.length === 0) {
    return "400";
  }
  try {
    const database = client.db(databaseName);
    const users = database.collection("users");

    const res = await users.updateOne(
      { username: username },
      { $pull: { liste: { [word]: { $exists: true } } } }
    );
    if (res.modifiedCount > 0) {
      return "200";
    } else {
      return "409";
    }
  } catch (error) {
    return "503";
  }
}

// Modifier un mot dans la liste de vocabulaire
app.put("/modifyWord", async (req, res) => {
  const headers = JSON.parse(JSON.stringify(req.headers));
  const token = headers.token;
  const user = await checkToken(token);
  if (user) {
    code_retour = await modifyWord(
      user.username,
      req.query.word1,
      req.query.word2
    );
  } else {
    code_retour = "403";
  }
  return res.send(code_retour);
});

async function modifyWord(username, word1, word2) {
  if (
    word1 === undefined ||
    word2 === undefined ||
    word1.length === 0 ||
    word2.length === 0
  ) {
    return "400";
  }
  try {
    const database = client.db(databaseName);
    const users = database.collection("users");

    const res = await users.updateOne(
      { username: username, ["liste." + word1]: { $exists: true } },
      { $set: { ["liste.$." + word1]: word2 } }
    );

    if (res.modifiedCount > 0) {
      return "200";
    } else {
      return "409";
    }
  } catch (error) {
    return "503";
  }
}

// Supprimer la liste de vocabulaire d'un utilisateur
app.put("/emptyList", async (req, res) => {
  const headers = JSON.parse(JSON.stringify(req.headers));
  const token = headers.token;
  const user = await checkToken(token);
  if (user) {
    code_retour = await emptyList(user.username);
  } else {
    code_retour = "403";
  }
  return res.send(code_retour);
});

async function emptyList(username) {
  try {
    const database = client.db(databaseName);
    const users = database.collection("users");
    const query = { username: username };
    const user = await users.findOne(query);

    const res = await users.updateOne(
      { username: username },
      { $set: { liste: [] } }
    );

    return "200";
  } catch (error) {
    return "503";
  }
}

// Mettre à jour la liste de vocabulaire d'un utilisateur
app.put("/update", async (req, res) => {
  const headers = JSON.parse(JSON.stringify(req.headers));
  const token = headers.token;
  const user = await checkToken(token);
  if (user) {
    code_retour = await update(user.username);
  } else {
    code_retour = "403";
  }
  return res.send(code_retour);
});

async function update(username) {
  try {
    const database = client.db(databaseName);
    const users = database.collection("users");
    const query = { username: username };
    const user = await users.findOne(query);
    var updatedListe = user.liste;
    var traduction = "";

    for (let i = 0; i < updatedListe.length; i++) {
      if (Object.values(updatedListe[i])[0] === "") {
        traduction = await translate(Object.keys(updatedListe[i])[0]);
        updatedListe[i] = { [Object.keys(updatedListe[i])[0]]: traduction };
      }
    }

    const res = await users.updateOne(
      { username: username },
      { $set: { liste: updatedListe } }
    );

    return "200";
  } catch (error) {
    return "503";
  }
}

app.get("/checkToken", async (req, res) => {
  try {
    const headers = JSON.parse(JSON.stringify(req.headers));
    const resp = await checkToken(headers.jwt);
    if (resp == null) {
      return res.send("401");
    } else {
      return res.send("200");
    }
  } catch (e) {
    console.log("Une erreur s'est produite : " + e.message);
    res.send("401");
  }
});

async function checkToken(token) {
  try {
    token = token.trim();
    jwt.verify(token, SECRET, (err, decodedToken) => {
      if (err) {
        return false;
      }
    });

    var decoded = jwt.decode(token);
    if (decoded !== null && decoded.exp * 1000 < Date.now()) {
      decoded = null;
    }
    return decoded;
  } catch (e) {
    return null;
  }
}

function hashPassword(password) {
  const hashedPassword = CryptoJS.SHA256(password).toString();
  return hashedPassword;
}

async function translate(text) {
  const apiKey = "319aff1a-cac2-bf9e-e34c-ee18669072c2:fx";
  const apiUrl = "https://api-free.deepl.com/v2/translate";

  const sourceLang = "FR";
  const targetLang = "EN";

  const response = await axios
    .post(apiUrl, null, {
      params: {
        auth_key: apiKey,
        text: text,
        source_lang: sourceLang,
        target_lang: targetLang,
      },
    })
    .catch((error) => {
      console.log(error);
    });
  return response.data.translations[0].text;
}

app.listen(3000, () => {
  console.log("Server listening...");
});

// Supprimer tout le contenu de la collection
app.delete("/deleteAll", async (req, res) => {
  code_retour = await resetCollection();
  return res.send(code_retour);
});

async function resetCollection() {
  try {
    const database = client.db(databaseName);
    const users = database.collection("users");
    const query = {};
    const response = await users.deleteMany(query);
    return "200"; // Collection réinitialisée
  } catch (error) {
    return "500"; // Erreur du serveur
  }
}
