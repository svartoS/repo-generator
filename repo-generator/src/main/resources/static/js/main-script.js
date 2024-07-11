$(document).ready(function () {
    let folderPath = '';
    let localRepositories = [];
    let remoteRepositories = [];
    let currentFilter = 'all'

    function fetchRepositories() {
        fetch('/repos')
            .then(response => response.json())
            .then(data => {
                remoteRepositories = data;
                displayRepositories();
            })
            .catch(error => {
                console.error('Ошибка получения списка удаленных репозиториев:', error);
            });
    }

    function fetchLocalRepositories() {
        fetch('/local-repos')
            .then(response => response.json())
            .then(data => {
                localRepositories = data;
                displayRepositories();
            })
            .catch(error => {
                console.error('Ошибка получения списка локальных репозиториев:', error);
            });
    }

    function displayRepositories() {
        const $tableBody = $('#repoTableBody');
        $tableBody.empty();

        let repositories = [];
        if (currentFilter === 'all') {
            repositories = [...localRepositories, ...remoteRepositories];
        } else if (currentFilter === 'local') {
            repositories = localRepositories;
        } else if (currentFilter === 'remote') {
            repositories = remoteRepositories;
        }

        repositories.forEach(repo => {
            const type = localRepositories.includes(repo) ? 'local' : 'remote';
            const icon = type === 'local' ? '💾' : '🌐';
            const description = repo.description || '';

            const statusId = `${type}-${repo.name}-status`;

            const syncButtonDisabled = type === 'remote' ? '' : 'disabled';
            const syncButtonClass = type === 'local' ? 'btn-secondary' : 'btn-success';

            const uploadButtonDisabled = type === 'remote' ? 'disabled' : '';
            const uploadButtonClass = type === 'local' ? 'btn-success' : 'btn-secondary';

            const row = `
                    <tr>
                        <td>${icon}</td>
                        <td>${repo.name}</td>
                        <td>${description}</td>
                        <td>
                                  <button class="btn btn-sm ${syncButtonClass} sync-local-btn"
                                          data-status-id="${statusId}"
                                          data-local-path="${repo.localPath}"
                                          ${syncButtonDisabled}>Sync</button>
                                  <button class="btn btn-sm ${uploadButtonClass} upload-remote-btn"
                                          data-status-id="${statusId}"
                                          data-local-path="${repo.localPath}"
                                          ${uploadButtonDisabled}>Upload</button>
                                </td>
                        <td><span id="${statusId}"></span></td>
                    </tr>
                `;

            $tableBody.append(row);
        });

        $tableBody.off('click', '.sync-local-btn').on('click', '.sync-local-btn', function (event) {
            const button = event.target;
            const localPath = encodeURIComponent(getFolderPath());
            const statusId = button.dataset.statusId;
            syncOrUploadRepository(localPath, 'sync', 'local', statusId);
        });

        $tableBody.off('click', '.upload-remote-btn').on('click', '.upload-remote-btn', function (event) {
            console.log('Upload Remote button clicked');
            const button = event.target;
            const localPath = encodeURIComponent(getFolderPath());
            const statusId = button.dataset.statusId;
            syncOrUploadRepository(localPath, 'upload', 'remote', statusId);
        });
    }

    function syncOrUploadRepository(localPath, operation, type, statusId) {
        const firstHyphenIndex = statusId.indexOf("-");
        const lastHyphenIndex = statusId.lastIndexOf("-");
        const repoName = statusId.substring(firstHyphenIndex + 1, lastHyphenIndex);

        console.log(statusId);
        console.log(type);
        const url = operation === 'sync' ? '/sync/local' : '/sync/upload';
        console.log(url);
        const method = operation === 'sync' ? 'POST' : 'PUT';
        console.log(method);

        const escapedStatusId = '#' + statusId.replace(/\./g, '\\.');
        console.log(repoName);

        fetch(url, {
            method: method,
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({localPath: localPath, repoName: repoName})
        })
            .then(response => {
                if (response.ok) {
                    if (type === 'local') {
                        $(`${escapedStatusId}`).html('<span class="text-success"><i class="fas fa-check-circle"></i> Синхронизировано</span>');
                    } else if (type === 'remote') {
                        $(`${escapedStatusId}`).html('<span class="text-success"><i class="fas fa-check-circle"></i> Загружено</span>');
                    }
                } else {
                    $(`${statusId}`).html('<span class="text-danger"><i class="fas fa-times-circle"></i> Ошибка</span>');
                }
            })
    }


    function syncAllLocalRepositories(localPath) {
        if (!localPath) {
            alert('Пожалуйста, укажите путь к папке с репозиториями.');
            return;
        }
        const encodedPath = encodeURIComponent(localPath);
        const url = '/sync/local/all';

        $('#syncAllStatus').text('⏳');

        fetch(url, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({localPath: encodedPath})
        })
            .then(response => {
                if (response.ok) {
                    $('#syncAllStatus').text('✅');
                } else {
                    $('#syncAllStatus').text('❌');
                }
            })
            .catch(error => {
                console.error('Ошибка синхронизации локальных репозиториев:', error);
                $('#syncAllStatus').text('❌');
            });
    }


    function uploadAllRepositories(localPath) {
        if (!localPath) {
            alert('Пожалуйста, укажите путь к папке с репозиториями.');
            return;
        }
        const encodedPath = encodeURIComponent(localPath);
        const url = '/sync/upload/all';

        $('#uploadAllStatus').text('⏳');

        fetch(url, {
            method: 'PUT',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({localPath: encodedPath})
        })
            .then(response => {
                if (response.ok) {
                    $('#uploadAllStatus').text('✅');
                } else {
                    $('#uploadAllStatus').text('❌');
                }
            })
            .catch(error => {
                console.error('Ошибка загрузки репозиториев:', error);
                $('#uploadAllStatus').text('❌');
            });
    }

    function loadRepositories() {
        syncAllLocalRepositories(getFolderPath());
    }

    function handleUpload() {
        uploadAllRepositories(getFolderPath());
    }

    function getFolderPath() {
        folderPath = document.getElementById('folderPath').value.trim();
        return folderPath;
    }

    function cycleRepositories() {
        if (currentFilter === 'all') {
            currentFilter = 'local';
        } else if (currentFilter === 'local') {
            currentFilter = 'remote';
        } else if (currentFilter === 'remote') {
            currentFilter = 'all';
        }
        displayRepositories();
    }

    window.handleUpload = handleUpload;
    window.cycleRepositories = cycleRepositories;
    window.getFolderPath = getFolderPath;
    window.loadRepositories = loadRepositories;
    window.fetchRepositories = fetchRepositories;
    window.fetchLocalRepositories = fetchLocalRepositories;
    window.syncAllLocalRepositories = syncAllLocalRepositories;

    fetchRepositories();
    fetchLocalRepositories();
});