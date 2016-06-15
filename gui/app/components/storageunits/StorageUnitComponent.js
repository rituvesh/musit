/**
 * Created by steinaol on 5/27/16.
 */

import React, { Component } from 'react';
import TextField from '../../components/musittextfield';
import MusitDropDown from '../../components/musitdropdownfield';
import { Panel, Form, Grid, Row, Col, } from 'react-bootstrap'
import Autosuggest from 'react-autosuggest'

export default class StorageUnitComponent extends Component {

  static propTypes = {
    unit: { storageUnitName: React.PropTypes.string,
            area: React.PropTypes.number,
            areal2: React.PropTypes.number,
            height: React.PropTypes.number,
            height2: React.PropTypes.number,
            storageType: React.PropTypes.string },
    updateType: React.PropTypes.func.isRequired,
    updateName: React.PropTypes.func.isRequired,
    updateHeight1: React.PropTypes.func.isRequired,
    updateHeight2: React.PropTypes.func.isRequired,
    updateAreal1: React.PropTypes.func.isRequired,
    updateAreal2: React.PropTypes.func.isRequired,
    suggest: React.PropTypes.array.isRequired,
    onAddressSuggestionsUpdateRequested: React.PropTypes.func.isRequired,
    onOrganizationSuggestionsUpdateRequested: React.PropTypes.func.isRequired,
    address: React.PropTypes.string,
  }

  static validateString(value, minimumLength = 3, maximumLength = 20) {
    const isSomething = value && value !== undefined ? value.length >= minimumLength : null
    const isValid = isSomething ? 'success' : null
    return value && value !== undefined && value.length > maximumLength ? 'error' : isValid
  }

  static validateNumber(value) {
    const isSomething = value && value !== undefined
    const isValid = isNaN(value) ? 'error' : 'success'
    return isSomething ? isValid : null
  }

  constructor(props) {
    super(props)

    this.areal = {
      controlId: 'areal1',
      controlId2: 'areal2',
      labelText: 'Areal (fra - til)',
      tooltip: 'Areal (fra - til)',
      valueType: 'text',
      placeHolderText: 'enter areal 1 here',
      placeHolderText2: 'enter areal 2 here',
      valueText: () => this.props.unit.area,
      valueText2: () => this.props.unit.areal2,
      validationState: () => StorageUnitComponent.validateNumber(this.props.unit.area),
      onChange: (area) => this.props.updateAreal1(area),
      onChange2: (areal2) => this.props.updateAreal2(areal2)
    }
    this.hoyde = {
      controlId: 'hoyde1',
      controlId2: 'hoyde2',
      labelText: 'Høyde(fra - til)',
      tooltip: 'Høyde (fra - til)',
      valueType: 'text',
      placeHolderText: 'enter høyde 1 here',
      placeHolderText2: 'enter høyde 2 here',
      valueText: () => this.props.unit.height,
      valueText2: () => this.props.unit.height2,
      validationState: () => StorageUnitComponent.validateNumber(this.props.unit.height),
      onChange: (height) => this.props.updateHeight1(height),
      onChange2: (height2) => this.props.updateHeight2(height2)
    }

    this.type = {
      controlId: 'storageUnitType',
      labelText: 'Type',
      items: ['storageunit', 'room', 'building', 'organization'],
      valueType: 'text',
      tooltip: 'Type lagringsenhet',
      placeHolderText: 'velg type here',
      valueText: () => this.props.unit.storageType,
      validationState: () => StorageUnitComponent.validateString(this.props.unit.storageType),
      onChange: (storageType) => this.props.updateType(storageType)
    }
    this.name = {
      controlId: 'name',
      labelText: 'Navn',
      tooltip: 'Navn',
      placeHolderText: 'enter name here',
      valueType: 'text',
      valueText: () => this.props.unit.storageUnitName,
      validationState: () => StorageUnitComponent.validateString(this.props.unit.storageUnitName),
      onChange: (storageUnitName) => this.props.updateName(storageUnitName)

    }

    this.address = {
      address: this.props.address
    }
    this.onAddressChange = this.onAddressChange.bind(this)
  }

  onAddressChange(event, { newValue }) {
    this.setState({
      address: newValue
    })
  }

  getAddressSuggestionValue(suggestion) {
    return `${suggestion.street} ${suggestion.streetNo}, ${suggestion.zip} ${suggestion.place}`
  }

  renderAddressSuggestion(suggestion) {
    const suggestionText = `${suggestion.street} ${suggestion.streetNo}, ${suggestion.zip} ${suggestion.place}`

    return (
      <span className={'suggestion-content'}>{suggestionText}</span>
    )
  }

  render() {
    const { address } = ''

    const inputAddressProps = {
      placeholder: 'addresse',
      value: address,
      type: 'search',
      onChange: this.onAddressChange
    }
    const {
      onAddressSuggestionsUpdateRequested,
      suggest } = this.props

    const addressBlock = (
      <Row className="row-centered">
        <Col md= {6}>
        <Autosuggest
          id={'addressField'}
          suggestions={suggest.addressField && suggest.addressField.data ? suggest.addressField.data : []}
          onSuggestionsUpdateRequested={onAddressSuggestionsUpdateRequested}
          getSuggestionValue={this.getAddressSuggestionValue}
          renderSuggestion={this.renderAddressSuggestion}
          inputProps={inputAddressProps}
        />
        </Col>
      </Row>
    )

    return (
        <div>
            <main>
                <Panel>
                    <Grid>
                        <Row className="row-centered">
                            <Col md={6}>
                                <Form horizontal>
                                  <MusitDropDown {...this.type} />
                                </Form>
                            </Col>
                            <Col md={6}>
                                <Form horizontal>
                                    <TextField {...this.name} />
                                </Form>
                            </Col>
                        </Row>
                        <Row styleClass="row-centered">
                            <Col md={6}>
                                <Form horizontal>
                                    <TextField {...this.areal} />
                                </Form>
                            </Col>
                            <Col md={6}>
                                <Form horizontal>
                                    <TextField {...this.hoyde} />
                                </Form>
                            </Col>
                        </Row >
                        { this.props.unit.storageType === 'building' ? addressBlock : null}
                    </Grid>
                </Panel>
            </main>
        </div>
        );
  }
}
