
const input = document.getElementById("message-input");
const messages = document.getElementById("messages");
const welcome = document.getElementById("welcome");
const sendButton = document.getElementById("send-button");
const chatContainer = document.getElementById("chat-container");

let loading = false;

function addMessage(role, content) {
    welcome.style.display = "none";

    const message = document.createElement("div");
    message.className = "message";

    const label = document.createElement("div");
    label.className = "message-label";
    label.textContent = role === "user" ? "Você" : "Assistente";

    const text = document.createElement("div");
    text.className = "message-content";

    // textContent evita interpretar respostas como HTML.
    text.textContent = content;

    if (role === "user") {
        text.classList.add("user-message");
    } else {
        text.classList.add("assistant-message");
    }

    message.appendChild(label);
    message.appendChild(text);

    messages.appendChild(message);

    chatContainer.scrollTop = chatContainer.scrollHeight;

    return message;
}

async function sendMessage() {
    const userMessage = input.value.trim();

    if (!userMessage || loading) return;

    loading = true;
    sendButton.disabled = true;

    addMessage("user", userMessage);

    input.value = "";
    input.style.height = "auto";

    const loadingMessage = addMessage(
        "assistant",
        "Pensando..."
    );

    loadingMessage.querySelector(".message-content")
        .classList.add("loading");

    try {
        const response = await fetch("/api/chat", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                mensagem: userMessage
            })
        });

        if (!response.ok) {
            throw new Error(
                `Erro HTTP ${response.status}: ${await response.text()}`
            );
        }

        const data = await response.json();
        const answer = data.content;

        loadingMessage.querySelector(".message-content")
            .classList.remove("loading");

        loadingMessage.querySelector(".message-content")
            .textContent = answer;

        loadHistory();

    } catch (error) {
        loadingMessage.querySelector(".message-content")
            .classList.remove("loading");

        loadingMessage.querySelector(".message-content")
            .textContent = "Não foi possível obter uma resposta: "
            + error.message;

    } finally {
        loading = false;
        sendButton.disabled = false;
        input.focus();
        chatContainer.scrollTop = chatContainer.scrollHeight;
    }
}

function suggestQuestion(question) {
    input.value = question;
    sendMessage();
}

function newChat() {
    messages.innerHTML = "";
    welcome.style.display = "block";
    input.value = "";
}

async function loadHistory() {
    try {
        const response = await fetch("/api/historico");

        if (!response.ok) {
            throw new Error("Falha ao carregar histórico.");
        }

        const history = await response.json();

        const historyList = document.getElementById("history-list");
        historyList.innerHTML = "";

        history.forEach(item => {
            const button = document.createElement("button");

            button.className = "history-item";
            button.textContent = item.pergunta;

            button.onclick = () => {
                messages.innerHTML = "";
                welcome.style.display = "none";

                addMessage("user", item.pergunta);
                addMessage("assistant", item.resposta);
            };

            historyList.appendChild(button);
        });

    } catch (error) {
        console.error("Erro ao carregar histórico:", error);
    }
}

input.addEventListener("keydown", event => {
    if (event.key === "Enter" && !event.shiftKey) {
        event.preventDefault();
        sendMessage();
    }
});

input.addEventListener("input", () => {
    input.style.height = "auto";
    input.style.height = input.scrollHeight + "px";
});

document.addEventListener("DOMContentLoaded", loadHistory);
