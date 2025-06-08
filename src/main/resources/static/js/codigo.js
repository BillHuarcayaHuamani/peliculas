document.getElementById("fechaHora").innerText = new Date().toLocaleString();

const videoPlayer = document.getElementById('moviePlayer');
const peliculaId = '[[${pelicula.id}]]'; 
const ultimaPosicionGuardada = parseFloat('[[${ultimaPosicion}]]'); 

if (ultimaPosicionGuardada > 0) {
    videoPlayer.currentTime = ultimaPosicionGuardada;
    console.log('Video reanudado desde:', ultimaPosicionGuardada, 'segundos');
}

let saveInterval;
const saveFrequency = 5;

function sendPlaybackPosition() {
    if (videoPlayer.currentTime > 0 && !videoPlayer.paused) {
        const currentPosition = videoPlayer.currentTime;
        fetch(`/api/peliculas/${peliculaId}/progreso`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ posicion: currentPosition })
        })
            .then(response => response.json())
            .then(data => {
                if (data.status === 'success') {
                } else {
                    console.error('Error al guardar progreso:', data.message);
                }
            })
            .catch(error => console.error('Error de red al guardar progreso:', error));
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
    fetch(`/api/peliculas/${peliculaId}/progreso`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ posicion: 0 }) });
});

window.addEventListener('beforeunload', () => {
    sendPlaybackPosition();
});
