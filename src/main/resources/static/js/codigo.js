document.getElementById("fechaHora").innerText = new Date().toLocaleString();

const videoPlayer = document.getElementById('moviePlayer');

const peliculaId = /*[[${jsPeliculaId}]]*/ 0;
const ultimaPosicionGuardada = /*[[${ultimaPosicion}]]*/ 0.0;


console.log('JS: ID de la película:', peliculaId);
console.log('JS: Última posición guardada:', ultimaPosicionGuardada);

if (videoPlayer && ultimaPosicionGuardada > 0) {
    videoPlayer.currentTime = ultimaPosicionGuardada;
    console.log('Video reanudado desde:', ultimaPosicionGuardada, 'segundos');
}


let saveInterval;
const saveFrequency = 5;

function sendPlaybackPosition() {
    if (videoPlayer && videoPlayer.currentTime > 0 && !videoPlayer.paused) {
        const currentPosition = videoPlayer.currentTime;
        console.log('JS: Posición actual a enviar:', currentPosition);

        if (typeof peliculaId !== 'number' || peliculaId <= 0) {
            console.error("JS: No se puede guardar el progreso. ID de película inválido:", peliculaId);
            return;
        }

        fetch(`/api/peliculas/${peliculaId}/progreso`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ posicion: currentPosition })
        })
            .then(response => {
                if (!response.ok) {
                    return response.text().then(text => {
                        throw new Error(`HTTP error! status: ${response.status}, message: ${text}`);
                    });
                }
                return response.json();
            })
            .then(data => {
                if (data.status === 'success') {
                    console.log('JS: Progreso guardado:', data.message);
                } else {
                    console.error('JS: Error al guardar progreso (servidor):', data.message);
                }
            })
            .catch(error => {
                console.error('JS: Error de red o al procesar respuesta:', error);
            });
    }
}

videoPlayer.addEventListener('play', () => {
    sendPlaybackPosition();
    saveInterval = setInterval(sendPlaybackPosition, saveFrequency * 1000);
});

videoPlayer.addEventListener('pause', () => {
    clearInterval(saveInterval);
    sendPlaybackPosition();
});

videoPlayer.addEventListener('ended', () => {
    clearInterval(saveInterval);
    sendPlaybackPosition();
});

window.addEventListener('beforeunload', () => {
    sendPlaybackPosition();
});
