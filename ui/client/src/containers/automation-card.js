import React, { useState } from 'react';
import { gql, useMutation } from '@apollo/client';
import styled from '@emotion/styled';
import { colors, mq } from '../styles';


const AutomationCard = ({ index, automation}) => {
  const { name, actionName, triggerName, autoEventName, actionEvent, interval, active } = automation;

  const [ removeAutomation ] = useMutation(DELETE_AUTOMATION_MUTATION);

  async function deleteAutomation() {
    console.log("CLICKED");
    await removeAutomation({ variables: { index } })
  }

  return (
    <CardContainer>
      <CardContent>
        <CardTitle>{name || ''}</CardTitle>
        <CardBody>
	  <CardColumns>
	    <CardHeading>Action name:</CardHeading>
	    <CardHeading>Trigger Name:</CardHeading>
	    <CardHeading>Auto Event Name:</CardHeading>
	    <CardHeading>Action Event:</CardHeading>
	    <CardHeading>Interval:</CardHeading>
	    <CardHeading>Active:</CardHeading>
	  </CardColumns>
	  <CardColumns>
	  	<CardHeading>&nbsp;&nbsp;</CardHeading>
	  </CardColumns>
	  <CardColumns>
	    <CardLines>{actionName}</CardLines>
	    <CardLines>{triggerName}</CardLines>
	    <CardLines>{autoEventName}</CardLines>
	    <CardLines>{actionEvent}</CardLines>
	    <CardLines>{interval}</CardLines>
	    <CardLines>{active.toString()}</CardLines>
	  </CardColumns>
        </CardBody>
      </CardContent>
    </CardContainer>
  );
};

function Example() {
  // Declare a new state variable, which we'll call "count"
  const [count, setCount] = useState(0);

  return (
    <div>
      <p>You clicked {count} times</p>
      <button onClick={() => setCount(count + 1)}>
        Click me
      </button>
    </div>
  );
}

export default AutomationCard;

const CardContainer = styled.div({
  borderRadius: 6,
  color: colors.text,
  backgroundSize: 'cover',
  backgroundColor: 'white',
  boxShadow: '0px 1px 5px 0px rgba(0,0,0,0.15)',
  backgroundPosition: 'center',
  display: 'flex',
  flexDirection: 'column',
  justifyContent: 'space-between',
  [mq[0]]: {
    width: '90%',
  },
  [mq[1]]: {
    width: '47%',
  },
  [mq[2]]: {
    width: '31%',
  },
  height: 180,
  margin: 10,
  overflow: 'hidden',
  position: 'relative',
  ':hover': {
    backgroundColor: colors.grey.light,
  },
  cursor: 'pointer',
});

const CardContent = styled.div({
  display: 'flex',
  flexDirection: 'column',
  justifyContent: 'space-around',
  height: '100%',
});

const CardColumns = styled.div({
  flexDirection: 'row',
});
const CardTitle = styled.h3({
  textAlign: 'center',
  fontSize: '1.4em',
  lineHeight: '1em',
  fontWeight: 700,
  color: colors.text,
  flex: 1,
});

const CardImageContainer = styled.div({
  height: 220,
  position: 'relative',
  '::after': {
    content: '""',
    position: 'absolute',
    top: 0,
    bottom: 0,
    left: 0,
    right: 0,
    background: 'rgba(250,0,150,0.20)',
  },
});

const CardImage = styled.img({
  objectFit: 'cover',
  width: '100%',
  height: '100%',
  filter: 'grayscale(60%)',
});

const CardBody = styled.div({
  padding: 18,
  flex: 1,
  display: 'flex',
  color: colors.textSecondary,
  flexDirection: 'row',
});

const CardFooter = styled.div({
  display: 'flex',
  flexDirection: 'Row',
});

const CardHeading = styled.div({
  display: 'flex',
  flexDirection: 'column',
  justifyContent: 'space-between',
  alignItems: 'flex-end',
  fontWeight: 700,
});

const CardLines = styled.div({
  display: 'flex',
  flexDirection: 'column',
  justifyContent: 'space-between',
  alignItems: 'flex-start'
});


export const DELETE_AUTOMATION_MUTATION= gql`
        mutation {
          deleteAutomation {
            index
          }
        }
`

