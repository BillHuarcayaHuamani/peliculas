
document.getElementById("fechaHora").innerText = new Date().toLocaleString();

const videoPlayer = document.getElementById('moviePlayer');

const peliculaId = videoPlayer ? parseInt(videoPlayer.dataset.movieId) : 0; 
const ultimaPosicionGuardada = videoPlayer ? parseFloat(videoPlayer.dataset.ultimaPosicion) : 0.0; 
const isFavoritedInitial = videoPlayer ? (videoPlayer.dataset.isFavorited === 'true') : false; 


console.log('JS: ID de la película:', peliculaId);
console.log('JS: Última posición guardada:', ultimaPosicionGuardada);
console.log('JS: Es favorita inicialmente:', isFavoritedInitial);

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

if (videoPlayer) {
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
}


const favoriteButton = document.getElementById('favoriteButton');

if (favoriteButton) {
    updateFavoriteButton(isFavoritedInitial);

    favoriteButton.addEventListener('click', () => {
        if (typeof peliculaId !== 'number' || peliculaId <= 0) {
            console.error("JS: No se puede alternar favorito. ID de película inválido:", peliculaId);
            return;
        }

        fetch(`/api/peliculas/${peliculaId}/toggle-favorito`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
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
                console.log('JS: Estado de favorito:', data.message);
                updateFavoriteButton(data.isFavorited); 
            } else {
                console.error('JS: Error al alternar favorito (servidor):', data.message);
            }
        })
        .catch(error => {
            console.error('JS: Error de red o al procesar respuesta de favorito:', error);
        });
    });

    function updateFavoriteButton(isFavorited) {
        const icon = favoriteButton.querySelector('i');
        const textSpan = favoriteButton.nextElementSibling && favoriteButton.nextElementSibling.querySelector('span'); 

        if (isFavorited) {
            icon.classList.remove('far', 'fa-heart');
            icon.classList.add('fas', 'fa-heart');
            favoriteButton.classList.add('favorited');
            if (textSpan) { 
                textSpan.textContent = '¡En tus favoritos!';
            }
        } else {
            icon.classList.remove('fas', 'fa-heart');
            icon.classList.add('far', 'fa-heart');
            favoriteButton.classList.remove('favorited');
            if (textSpan) { 
                textSpan.textContent = 'Marcar como favorito';
            }
        }
    }
}
