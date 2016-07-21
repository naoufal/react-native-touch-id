
import {
  Platform
} from 'react-native'

module.exports = Platform.OS === 'android' ? require('./LocalAuth.android') : require('./LocalAuth.ios')
