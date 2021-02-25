import React, { Fragment } from 'react';
import { Router } from '@reach/router';
/** importing our pages */
import Automations from './automations';

export default function Pages() {
  return (
    <Router primary={false} component={Fragment}>
      <Automations path="/" />
    </Router>
  );
}
