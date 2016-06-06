const LOAD = 'musit/language/LOAD'
const LOAD_SUCCESS = 'musit/language/LOAD_SUCCESS'
const LOAD_FAIL = 'musit/language/LOAD_FAIL'


const initialState = {
  loaded: false
};

const languageReducer = (state = initialState, action = {}) => {
  switch (action.type) {
    case LOAD:
      return {
        ...state,
        loading: true
      }
    case LOAD_SUCCESS:
      return {
        ...state,
        loading: false,
        loaded: true,
        data: action.result
      }
    case LOAD_FAIL:
      return {
        ...state,
        loading: false,
        loaded: false,
        error: action.error
      }
    default:
      return state
  }
}

export default languageReducer

export const isLoaded = (globalState) => {
  return globalState.language && globalState.language.loaded
}

export const load = () => {
  return {
    types: [LOAD, LOAD_SUCCESS, LOAD_FAIL],
    promise: (client) => client.get('/loadLanguage')
  }
}
