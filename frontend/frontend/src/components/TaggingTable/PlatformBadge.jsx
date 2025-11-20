import React from 'react';

const PlatformBadge = ({ platform }) => {
  const getBadgeStyle = (platform) => {
    const styles = {
      'WEB': { bg: 'bg-primary', text: 'white', icon: '🌐' },
      'Android': { bg: 'bg-success', text: 'white', icon: '🤖' },
      'iOS': { bg: 'bg-info', text: 'white', icon: '📱' }
    };
    return styles[platform] || { bg: 'bg-secondary', text: 'white', icon: '❓' };
  };

  const style = getBadgeStyle(platform);
  
  if (!platform) return null;
  
  return (
    <span className={`badge ${style.bg} text-${style.text} px-2 py-1`} style={{ fontSize: '0.75rem' }}>
      {style.icon} {platform}
    </span>
  );
};

export default PlatformBadge;

