import React, { Component, PropTypes } from 'react'
import { Button } from 'react-bootstrap'
import FontAwesome from 'react-fontawesome'

export default class ObservationControlComponent extends Component {
  static propTypes = {
    id: PropTypes.number.isRequired,
    translate: PropTypes.func.isRequired,
    onClickNewObservation: PropTypes.func.isRequired,
    onClickNewControl: PropTypes.func.isRequired,
  }

  render() {
    const { id, translate, onClickNewControl, onClickNewObservation } = this.props
    const getTranslate = (term) => (translate(`musit.leftmenu.observationControl.${term}`))
    const buttonLogic = (type, eventType) => {
      return (
        <Button
          id={`${id}_${type}`}
          onClick={(event) => eventType(event.target.value)}
          style={{ width: '100%', textAlign: 'left' }}
        >
          <FontAwesome name="plus-circle" style={{ padding: '2px' }} />
          {getTranslate(type)}
        </Button>
      )
    }
    return (
      <div>
        {buttonLogic('newObservation', onClickNewObservation)}
        {buttonLogic('newControl', onClickNewControl)}
      </div>
    )
  }
}