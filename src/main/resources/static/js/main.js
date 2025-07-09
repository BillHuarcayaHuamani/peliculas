
document.addEventListener('DOMContentLoaded', () => {
    const renderMovieCard = (movie) => {
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

    const searchInput = document.getElementById('searchInput');
    const searchResultsDiv = document.getElementById('searchResults');
    const noSearchResultsP = document.getElementById('noSearchResults');
    let searchTimeout;

    const performSearch = async () => {
        const query = searchInput.value.trim();
        if (query.length < 2) { 
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

    searchInput.addEventListener('keyup', () => {
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(performSearch, 300); 
    });

    const recommendationsResultsDiv = document.getElementById('recommendationsResults');
    const noRecommendationsP = document.getElementById('noRecommendations');

    const fetchRecommendations = async () => {
        try {
            const response = await fetch('/api/movies/recommendations');
            if (!response.ok) {
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
                noRecommendationsP.style.display = 'block'; 
            }
        } catch (error) {
            console.error("Error al cargar las recomendaciones:", error);
            recommendationsResultsDiv.innerHTML = '<p class="text-danger text-center">Error al cargar las recomendaciones.</p>';
            noRecommendationsP.style.display = 'none';
        }
    };

    fetchRecommendations();

});

document.getElementById("fechaHora").innerText = new Date().toLocaleString();
