import { StreamChat } from 'stream-chat';
import { Channel, ChannelList, Chat, MessageInput, MessageList, Window } from 'stream-chat-react';

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

export const AppChat = () => {
    return (
        <Chat client={chatClient} theme="messaging dark">
            <ChannelList />
            <Channel>
                <Window>
                    <MessageList />
                    <MessageInput />
                </Window>
            </Channel>
        </Chat>
    );
};
