import React from 'react';
import { LANGUAGE_LABELS, LanguageType } from '../model/RemoteExecutable';
import { useUploadViewModel } from '../viewmodel/useUploadViewModel';
import './UploadView.css';

interface UploadViewProps {
  onBack: () => void;
}

export const UploadView: React.FC<UploadViewProps> = ({ onBack }) => {
  const {
    selectedFile,
    selectedLanguage,
    setSelectedLanguage,
    uploadState,
    isDragging,
    fileInputRef,
    handleDrop,
    handleDragOver,
    handleDragLeave,
    handleFileInputChange,
    openFilePicker,
    upload,
    reset,
  } = useUploadViewModel();

  const isUploading = uploadState.status === 'uploading';
  const isSuccess   = uploadState.status === 'success';
  const isError     = uploadState.status === 'error';

  return (
    <div className="upload-view">
      <header className="upload-view__header">
        <button className="upload-view__back-btn" onClick={onBack} aria-label="Wróć">
          ←
        </button>
        <h1 className="upload-view__title">Prześlij plik</h1>
      </header>

      <main className="upload-view__main">
        {/* DROP ZONE */}
        <div
          className={[
            'upload-view__dropzone',
            isDragging  ? 'upload-view__dropzone--active'   : '',
            selectedFile ? 'upload-view__dropzone--has-file' : '',
          ].join(' ')}
          onDrop={handleDrop}
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onClick={selectedFile ? undefined : openFilePicker}
          role="button"
          tabIndex={0}
          onKeyDown={(e) => e.key === 'Enter' && openFilePicker()}
          aria-label="Strefa przeciągania pliku"
        >
          <input
            ref={fileInputRef}
            type="file"
            className="upload-view__file-input"
            onChange={handleFileInputChange}
            aria-hidden="true"
          />

          {selectedFile ? (
            <div className="upload-view__file-info">
              <span className="upload-view__file-icon">📄</span>
              <div className="upload-view__file-details">
                <p className="upload-view__file-name">{selectedFile.name}</p>
                <p className="upload-view__file-size">
                  {(selectedFile.size / 1024).toFixed(1)} KB
                </p>
              </div>
              <button
                className="upload-view__remove-btn"
                onClick={(e) => { e.stopPropagation(); reset(); }}
                aria-label="Usuń plik"
              >
                ✕
              </button>
            </div>
          ) : (
            <div className="upload-view__drop-prompt">
              <span className="upload-view__drop-icon">⬆</span>
              <p className="upload-view__drop-text">
                {isDragging ? 'Upuść plik tutaj' : 'Przeciągnij plik lub kliknij, aby wybrać'}
              </p>
              <p className="upload-view__drop-hint">Dowolny typ pliku</p>
            </div>
          )}
        </div>

        {/* LANGUAGE SELECT */}
        <div className="upload-view__field">
          <label className="upload-view__label" htmlFor="language-select">
            Język / typ
          </label>
          <select
            id="language-select"
            className="upload-view__select"
            value={selectedLanguage}
            onChange={(e) => setSelectedLanguage(e.target.value as LanguageType)}
            disabled={isUploading}
          >
            {Object.entries(LANGUAGE_LABELS).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </div>

        {/* PROGRESS BAR */}
        {isUploading && (
          <div
            className="upload-view__progress"
            role="progressbar"
            aria-valuenow={uploadState.progress}
            aria-valuemin={0}
            aria-valuemax={100}
          >
            <div
              className="upload-view__progress-fill"
              style={{ width: `${uploadState.progress}%` }}
            />
          </div>
        )}

        {/* STATUS */}
        {isSuccess && (
          <div className="upload-view__status upload-view__status--success">
            ✓ {uploadState.message ?? 'Plik przesłany pomyślnie.'}
          </div>
        )}
        {isError && (
          <div className="upload-view__status upload-view__status--error">
            ✕ {uploadState.message ?? 'Wystąpił błąd.'}
          </div>
        )}

        {/* ACTIONS */}
        <div className="upload-view__actions">
          {isSuccess ? (
            <button className="upload-view__btn upload-view__btn--secondary" onClick={reset}>
              Prześlij kolejny plik
            </button>
          ) : (
            <button
              className="upload-view__btn upload-view__btn--primary"
              onClick={upload}
              disabled={!selectedFile || isUploading}
            >
              {isUploading ? 'Przesyłanie…' : 'Prześlij'}
            </button>
          )}
        </div>
      </main>
    </div>
  );
};