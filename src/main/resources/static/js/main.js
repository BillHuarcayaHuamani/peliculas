// src/main/resources/static/js/main.js

document.addEventListener('DOMContentLoaded', () => {
    // Función para renderizar una tarjeta de película
    const renderMovieCard = (movie) => {
        // Asegúrate de que la URL de la portada sea correcta (maneja si es null o vacía)
        const imageUrl = movie.portada && movie.portada !== '' ? movie.portada : '/assets/img/portadaDefecto.png';
        return `
            <div class="movie-card-dynamic">
                <a href="/pelicula/${movie.id}">
                    <img src="${imageUrl}" alt="${movie.titulo}" class="img-fluid">
                    <h5 class="card-title">${movie.titulo}</h5>
                </a>
            </div>
        `;
    };

    // --- Lógica de Búsqueda ---
    const searchInput = document.getElementById('searchInput');
    const searchResultsDiv = document.getElementById('searchResults');
    const noSearchResultsP = document.getElementById('noSearchResults');
    let searchTimeout;

    const performSearch = async () => {
        const query = searchInput.value.trim();
        if (query.length < 2) { // Mínimo 2 caracteres para buscar
            searchResultsDiv.innerHTML = '';
            noSearchResultsP.style.display = 'none';
            return;
        }

        try {
            const response = await fetch(`/api/movies/search?query=${encodeURIComponent(query)}`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const movies = await response.json();

            if (movies.length > 0) {
                searchResultsDiv.innerHTML = movies.map(renderMovieCard).join('');
                noSearchResultsP.style.display = 'none';
            } else {
                searchResultsDiv.innerHTML = '';
                noSearchResultsP.style.display = 'block';
            }
        } catch (error) {
            console.error("Error al realizar la búsqueda:", error);
            searchResultsDiv.innerHTML = '<p class="text-danger text-center">Error al cargar los resultados de búsqueda.</p>';
            noSearchResultsP.style.display = 'none';
        }
    };

    // Evento keyup para iniciar la búsqueda con un pequeño debounce
    searchInput.addEventListener('keyup', () => {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(performSearch, 300); // Retraso de 300ms
    });

    // --- Lógica de Recomendaciones ---
    const recommendationsResultsDiv = document.getElementById('recommendationsResults');
    const noRecommendationsP = document.getElementById('noRecommendations');

    const fetchRecommendations = async () => {
        try {
            const response = await fetch('/api/movies/recommendations');
            if (!response.ok) {
                // Si el usuario no está autenticado, el backend devolverá un 401 o 403.
                // Manejamos esto mostrando el mensaje de "inicia sesión".
                if (response.status === 401 || response.status === 403) {
                    recommendationsResultsDiv.innerHTML = '';
                    noRecommendationsP.style.display = 'block';
                    return;
                }
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const movies = await response.json();

            if (movies.length > 0) {
                recommendationsResultsDiv.innerHTML = movies.map(renderMovieCard).join('');
                noRecommendationsP.style.display = 'none';
            } else {
                recommendationsResultsDiv.innerHTML = '';
                noRecommendationsP.style.display = 'block'; // Mostrar mensaje si no hay recomendaciones
            }
        } catch (error) {
            console.error("Error al cargar las recomendaciones:", error);
            recommendationsResultsDiv.innerHTML = '<p class="text-danger text-center">Error al cargar las recomendaciones.</p>';
            noRecommendationsP.style.display = 'none';
        }
    };

    // Cargar recomendaciones al cargar la página
    fetchRecommendations();

    // Nota: Para que las recomendaciones se actualicen dinámicamente después de ver una película,
    // necesitarías una forma de disparar `fetchRecommendations()` desde la página de detalle
    // o al volver a la página principal. Una solución simple es recargar la página principal
    // después de completar una visualización, o implementar un sistema de eventos más complejo.
    // Por ahora, se cargan solo al inicio.
});

// Script para mostrar la fecha y hora en el header (ya existía)
document.getElementById("fechaHora").innerText = new Date().toLocaleString();
