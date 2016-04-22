/*
 *  MUSIT is a museum database to archive natural and cultural history data.
 *  Copyright (C) 2016  MUSIT Norway, part of www.uio.no (University of Oslo)
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License,
 *  or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

import * as types from '../../constants/example';

export const updateState1 = (state) => {
	  return {
  type: types.UPDATE_STATE1_MESSAGE,
  text: state
	};
};

export const updateState2 = (state) => {
	  return {
  type: types.UPDATE_STATE2_MESSAGE,
  text: state
	};
};

export const sendRestCallExample = (username, text) => {
	  return fetch(`http://localhost:8080/v1/chat/${username}`, {
		  mode: 'no-cors',
		  method: 'POST',
		  body: text,
		  headers: {
			'Accept': 'text/plain',
			'Content-Type': 'text/plain'
		}
	}).then(
		messages => console.log(messages)
	).catch(
		error => console.log(error)
	);
};