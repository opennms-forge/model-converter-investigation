import React from 'react';
import styled from '@emotion/styled';
import { colors } from '../styles';

const Footer = ({ children }) => {
  return (
    <FooterContainer>
    </FooterContainer>
  );
};

export default Footer;

/** Footer styled components */
const FooterContainer = styled.div({
  display: 'flex',
  flexDirection: 'row',
  justifyContent: 'center',
  alignItems: 'center',
  color: '#217BB7',
  marginTop: 30,
  height: 50,
  padding: 20,
  backgroundColor: 'white',
  borderTop: `solid 1px ${colors.black}`,
});

