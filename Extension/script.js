document.getElementById("addBtn").onclick = function () {
  var wordInput = document.getElementById("word");
  var wordValue = wordInput.value;
  var jwt = localStorage.getItem("jwt");

  fetch("http://localhost:3000/checkToken", {
    method: "GET",
    headers: {
      jwt: jwt,
    },
  })
    .then((response) => {
      return response.text();
    })
    .then((text) => {
        console.log(text);
      if (text === "200") {
        addWord(wordValue, jwt)
      } else {
        showMessage("Vous n'êtes pas connecté");
        localStorage.clear();
      }
      setTimeout(hideMessage, 2000);
    })
    .catch((error) => {
      console.log("Erreur:", error);
    });

  function addWord(wordValue, jwt) {
    fetch("http://localhost:3000/addWord?word=" + wordValue, {
      method: "PUT",
      headers: {
        token: jwt,
      },
    })
      .then((response) => {
        return response.text();
      })
      .then((text) => {
        if (text === "200") {
          showMessage("Le mot " + wordValue + " a été ajouté à la liste");
        } else if (text === "409") {
          showMessage("Le mot " + wordValue + " est déjà dans la liste");
        } else if (text === "400") {
          showMessage("Veuillez entrer un mot");
        } else {
          showMessage("Erreur lors de l'ajout du mot");
        }
        setTimeout(hideMessage, 2000);
      })
      .catch((error) => {
        console.log("Erreur:", error);
      });
  }

  function showMessage(message) {
    var messageElement = document.getElementById("message");
    messageElement.innerHTML = message;
    messageElement.style.display = "block";
  }

  function hideMessage() {
    var messageElement = document.getElementById("message");
    messageElement.style.display = "none";
  }
};

document.getElementById("loginBtn").addEventListener("click", function () {
  window.open("login.html");
});
