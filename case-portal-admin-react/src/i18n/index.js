import i18n from 'i18next';
import translations from './translations';

var lang = 'en';
var fallbackLng = 'en';

if (navigator.language === 'pt-BR') {
    lang = 'ptBR';
}

i18n.init({
    resources: translations,
    lng: lang,
    fallbackLng: fallbackLng,
    interpolation: {
        escapeValue: false
    }
});

export default i18n;
