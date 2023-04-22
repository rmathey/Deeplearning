document
  .getElementById("connectionButton")
  .addEventListener("click", function () {
    localStorage.clear();
    console.log(document.getElementById("username").value);
    console.log(document.getElementById("password").value);
    fetch("http://localhost:3000/signin", {
      method: "GET",
      headers: {
        username: document.getElementById("username").value,
        password: document.getElementById("password").value,
      },
    })
      .then((response) => {
        return response.text();
      })
      .then((text) => {
        console.log(text);
        if (text !== "404") {
          localStorage.setItem("jwt", text);
          setTimeout(closeWindow, 2000);
          showMessage("Connexion réussie !");
        } else {
          showMessage("La connexion a échoué");
        }
      })
      .catch((error) => {
        console.log("Erreur:", error);
      });

    function showMessage(message) {
      var messageElement = document.getElementById("message2");
      messageElement.innerHTML = message;
      messageElement.style.display = "block";
    }

    function closeWindow() {
      window.close();
    }
  });
