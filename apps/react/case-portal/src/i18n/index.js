import i18n from 'i18next'
import ptBR from './pt_br'
import enUS from './en_us'

var lang = 'en'
var fallbackLng = 'en'

if (navigator.language === 'pt-BR') {
  lang = 'ptBR'
}

i18n.init({
  resources: {
    en: {
      translation: enUS,
    },
    ptBR: {
      translation: ptBR,
    },
  },
  lng: lang,
  fallbackLng: fallbackLng,
  interpolation: {
    escapeValue: false,
  },
})

export default i18n
