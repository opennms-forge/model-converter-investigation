import React from 'react';
import { useQuery, writeQuery, gql, useMutation } from '@apollo/client';
import AutomationCard from '../containers/automation-card';
import QueryResult from '../components/query-result';

/**
 * Display a grid of automations 
 */
const Automations = () => {
  const { loading, error, data } = useQuery(AUTOMATIONS);
  
  if (loading) return 'Loading...';
  
  if (error) return `Received error! ${error.message}`;
  return (
  
	<QueryResult error={error} loading={loading} data={data}>
	  {data?.automations?.map((automation, index) => (
	    <AutomationCard key={index} index={index} automation={automation}/>
	  ))}
	</QueryResult>
  );
};

const DelAutomation = () => {
  const [ delAutomation, { data } ] = writeQuery(DELETE_AUTOMATION_MUTATION);
  
  return (
    <div>
      <form onSubmit={e => {
	                e.preventDefault();
        delAutomation({ variables: { index: "0" } });
          // input.value = '';
        }}
      >
        <input/>
        <button type="submit">Delete</button>
      </form>
    </div>
  )
};

export const AUTOMATIONS = gql`
	query Query {
	  automations {
	    name
            interval
	    actionName
	    triggerName
	    active
	    autoEventName
	    actionEvent
	  }
	}
`


export const DELETE_AUTOMATION_MUTATION= gql`
	mutation Mutation {
	  deleteAutomation {
	    index
	  }
	}
`

export default Automations;
