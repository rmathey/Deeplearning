// Codes d’état HTTP
// https://learn.microsoft.com/fr-fr/partner-center/developer/error-codes

const express = require("express");
const bodyParser = require("body-parser");
const app = express();
const axios = require("axios");
const jwt = require("jsonwebtoken");

const { MongoClient, ServerApiVersion } = require("mongodb");
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

app.use(
  "/api-docs",
  swaggerUi.serve,
  swaggerUi.setup(swaggerDocument, options)
);
app.use(cors());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

app.put("/signup", async (req, res) => {
  const headers = JSON.parse(JSON.stringify(req.headers));
  message_retour = await createUser(headers.username, headers.password);
  return res.json({ message: message_retour });
});

app.get("/signin", async (req, res) => {
  const headers = JSON.parse(JSON.stringify(req.headers));
  const token = await authentification(headers.username, headers.password);
  if (token) {
    message_retour = token;
  } else {
    message_retour = "Username or password incorrect";
  }
  return res.send(message_retour);
});

app.put("/addWord", async (req, res) => {
  const headers = JSON.parse(JSON.stringify(req.headers));
  const token = headers.token;
  const user = await checkToken(token);
  if (user) {
    message_retour = await addWord(user.username, req.query.word);
  } else {
    message_retour = "Token invalide";
  }
  return res.json({ message: message_retour });
});

app.put("/delWord", async (req, res) => {
  const headers = JSON.parse(JSON.stringify(req.headers));
  const token = headers.token;
  const user = await checkToken(token);
  if (user) {
    message_retour = await delWord(user.username, req.query.word);
  } else {
    message_retour = "Token invalide";
  }
  return res.json({ message: message_retour });
});

app.put("/modifyWord", async (req, res) => {
  const headers = JSON.parse(JSON.stringify(req.headers));
  const token = headers.token;
  const user = await checkToken(token);
  if (user) {
    message_retour = await modifyWord(
      user.username,
      req.query.word1,
      req.query.word2
    );
  } else {
    message_retour = "Token invalide";
  }
  return res.json({ message: message_retour });
});

app.get("/getInfo", async (req, res) => {
  const headers = JSON.parse(JSON.stringify(req.headers));
  const token = headers.token;
  const user = await checkToken(token);
  if (user) {
    message_retour = await getUser(user.username);
  } else {
    message_retour = "401";
  }
  return res.send(message_retour);
});

app.put("/emptyList", async (req, res) => {
  const headers = JSON.parse(JSON.stringify(req.headers));
  const token = headers.token;
  const user = await checkToken(token);
  if (user) {
    message_retour = await emptyList(user.username);
  } else {
    message_retour = "Token invalide";
  }
  return res.json({ message: message_retour });
});

app.get("/trad", async (req, res) => {
  const message_retour = await translate(req.query.sentence);
  return res.json({ message: message_retour });
});

app.put("/update", async (req, res) => {
  const headers = JSON.parse(JSON.stringify(req.headers));
  const token = headers.token;
  const user = await checkToken(token);
  if (user) {
    message_retour = await update(user.username);
  } else {
    message_retour = "Token invalide";
  }
  return res.json({ message: message_retour });
});

app.get("/checkToken", async (req, res) => {
  try {
    const headers = JSON.parse(JSON.stringify(req.headers));
    message_retour = await checkToken(headers.jwt);
    if (message_retour == null) {
        return res.send("401");
    }
    else {
        return res.send("200");
    }
  } catch (e) {
    console.log("Une erreur s'est produite : " + e.message);
    res.send("401");
  }
});

app.listen(3000, () => {
  console.log("Server listening...");
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
      return "200";
    } else {
      return "409";
    }
  } catch (error) {
    return "503";
  }
}

async function addWord(username, word) {
  if (word.length === 0) {
    return "Encoder entrer un mot";
  }
  try {
    const database = client.db(databaseName);
    const users = database.collection("users");

    const userItem = await getUser(username);

    console.log(userItem.liste);
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
      return word + " a été rajouté dans la liste";
    } else {
      return word + " est déja dans la liste";
    }
  } catch (error) {
    return "503";
  }
}

async function delWord(username, word) {
  if (word.length === 0) {
    return "Encoder entrer un mot";
  }
  try {
    const database = client.db(databaseName);
    const users = database.collection("users");

    const res = await users.updateOne(
      { username: username },
      { $pull: { liste: { [word]: { $exists: true } } } }
    );
    return res;
  } catch (error) {
    return "503";
  }
}

async function modifyWord(username, word1, word2) {
  if (word1.length === 0 && word2.length === 0) {
    return "Encoder entrer 2 mots valides";
  }
  try {
    const database = client.db(databaseName);
    const users = database.collection("users");

    const res = await users.updateOne(
      { username: username, ["liste." + word1]: { $exists: true } },
      { $set: { ["liste.$." + word1]: word2 } }
    );

    return res;
  } catch (error) {
    return "503";
  }
}

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

async function authentification(username, password) {
  var user = await getUser(username);
  console.log(user);
  if (user === null) {
    return false;
  }
  if (user.username == username && user.password == password) {
    const token = jwt.sign(
      {
        id: user.id,
        username: user.username,
      },
      SECRET,
      { expiresIn: "24h" }
    );
    return token;
  } else {
    return false;
  }
}

async function checkToken(token) {
  token = token.trim();
  jwt.verify(token, SECRET, (err, decodedToken) => {
    if (err) {
      return false;
    }
  });

  const decoded = jwt.decode(token);
  return decoded;
}

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
