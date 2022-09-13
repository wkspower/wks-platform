import { useEffect, useRef, useState } from 'react';
import { StreamChat } from 'stream-chat';
import { Channel, ChannelHeader, Chat, MessageInput, MessageList, Thread, Window } from 'stream-chat-react';

import 'stream-chat-react/dist/css/index.css';

const chatClient = StreamChat.getInstance('qpkb2vb8bnhm');
const userToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiZnJhbmNhdiJ9.4xL0BOkQkVBq98w8NDonVciV_e5ys1wcXFx79qJ2to8';

chatClient.connectUser(
    {
        id: 'francav',
        name: 'francav',
        image: 'https://getstream.io/random_png/?id=restless-cell-5&name=restless-cell-5'
    },
    userToken
);

export const CaseChat = ({ channelName }) => {
    const [channel, setChannel] = useState(null);

    useEffect(() => {
        setChannel(chatClient.channel('messaging', channelName, {}));
    }, [channelName]);

    return (
        <Chat client={chatClient} theme="messaging dark">
            <Channel channel={channel}>
                <Window>
                    {/* <ChannelHeader /> */}
                    <MessageList />
                    <MessageInput />
                </Window>
                <Thread />
            </Channel>
        </Chat>
    );
};
